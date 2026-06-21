#!/usr/bin/env python3
"""校验 Synapse Framework 的 reactor、BOM、文档、自动配置和依赖边界。"""

from __future__ import annotations

import sys
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
NS = {"m": "http://maven.apache.org/POM/4.0.0"}
SYNAPSE_GROUP = "com.indigo.synapse"


@dataclass(frozen=True)
class MavenProject:
    """从 reactor POM 推导出的项目事实。"""

    path: Path
    artifact_id: str
    packaging: str
    dependencies: frozenset[tuple[str, str]]


class ArchitectureVerifier:
    """收集所有违规后统一报告，避免一次只修复一个错误。"""

    def __init__(self) -> None:
        self.errors: list[str] = []
        self.projects: dict[str, MavenProject] = {}

    def verify(self) -> None:
        self._load_reactor(ROOT / "pom.xml", is_root=True)
        self._verify_bom()
        self._verify_readme_and_docs()
        self._verify_auto_configuration_imports()
        self._verify_deleted_modules()
        self._verify_dependency_boundaries()
        self._verify_web_context_trust_boundaries()
        self._verify_aop_infrastructure_boundaries()

    def _load_reactor(self, pom: Path, *, is_root: bool = False) -> None:
        if not pom.is_file():
            self.errors.append(f"缺少 reactor POM: {pom.relative_to(ROOT)}")
            return
        root = ET.parse(pom).getroot()
        artifact_id = self._required_text(root, "m:artifactId", pom)
        packaging = self._text(root, "m:packaging") or "jar"
        dependencies = frozenset(
            (self._text(node, "m:groupId") or "", self._text(node, "m:artifactId") or "")
            for node in root.findall("m:dependencies/m:dependency", NS)
        )
        project = MavenProject(pom.parent, artifact_id, packaging, dependencies)
        if artifact_id in self.projects:
            self.errors.append(f"重复 artifactId: {artifact_id}")
        self.projects[artifact_id] = project
        if not is_root and pom.parent.name != artifact_id:
            self.errors.append(
                f"目录名与 artifactId 不一致: {pom.parent.relative_to(ROOT)} != {artifact_id}"
            )
        for module in root.findall("m:modules/m:module", NS):
            module_name = (module.text or "").strip()
            module_dir = pom.parent / module_name
            if not module_dir.is_dir():
                self.errors.append(f"Reactor 模块目录不存在: {module_dir.relative_to(ROOT)}")
                continue
            self._load_reactor(module_dir / "pom.xml")

    def _verify_bom(self) -> None:
        jar_artifacts = {
            artifact for artifact, project in self.projects.items()
            if project.packaging == "jar"
        }
        bom_root = ET.parse(ROOT / "synapse-bom/pom.xml").getroot()
        bom_artifacts = {
            self._text(node, "m:artifactId") or ""
            for node in bom_root.findall("m:dependencyManagement/m:dependencies/m:dependency", NS)
            if self._text(node, "m:groupId") == SYNAPSE_GROUP
        }
        self._compare_sets("BOM 缺少正式 JAR", jar_artifacts - bom_artifacts)
        self._compare_sets("BOM 包含不存在或非 JAR 模块", bom_artifacts - jar_artifacts)

    def _verify_readme_and_docs(self) -> None:
        readme = (ROOT / "README.md").read_text(encoding="utf-8")
        for artifact, project in sorted(self.projects.items()):
            if project.packaging != "jar":
                continue
            if f"`{artifact}`" not in readme:
                self.errors.append(f"README 当前模块表缺少: {artifact}")
            module_doc = ROOT / "docs/modules" / f"{artifact}.md"
            if not module_doc.is_file():
                self.errors.append(f"正式 JAR 缺少模块文档: {module_doc.relative_to(ROOT)}")

    def _verify_auto_configuration_imports(self) -> None:
        relative = Path("src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")
        seen_global: set[str] = set()
        for project in self.projects.values():
            imports_file = project.path / relative
            if not imports_file.is_file():
                continue
            entries = [
                line.strip() for line in imports_file.read_text(encoding="utf-8").splitlines()
                if line.strip() and not line.lstrip().startswith("#")
            ]
            duplicates = {entry for entry in entries if entries.count(entry) > 1}
            self._compare_sets(f"AutoConfiguration.imports 存在重复项 ({project.artifact_id})", duplicates)
            for class_name in entries:
                source = project.path / "src/main/java" / Path(*class_name.split(".")).with_suffix(".java")
                if not source.is_file():
                    self.errors.append(
                        f"AutoConfiguration 类不存在: {project.artifact_id}: {class_name}"
                    )
                key = f"{project.artifact_id}:{class_name}"
                if key in seen_global:
                    self.errors.append(f"AutoConfiguration 重复注册: {key}")
                seen_global.add(key)

    def _verify_deleted_modules(self) -> None:
        deleted = ("synapse-cloud", "synapse-file", "synapse-mq")
        for name in deleted:
            if (ROOT / name).exists():
                self.errors.append(f"已删除模块目录仍存在: {name}")
        active_files = list(ROOT.glob("**/pom.xml"))
        active_files += list(ROOT.glob("**/src/main/**/*.java"))
        active_files += list(ROOT.glob("**/src/main/**/*.properties"))
        for path in active_files:
            if "target" in path.parts:
                continue
            text = path.read_text(encoding="utf-8", errors="ignore")
            for name in deleted:
                if name in text:
                    self.errors.append(f"有效构建或生产代码仍引用 {name}: {path.relative_to(ROOT)}")

    def _verify_dependency_boundaries(self) -> None:
        all_poms = [project.path / "pom.xml" for project in self.projects.values()]
        for pom in all_poms:
            if "com.alibaba.cloud" in pom.read_text(encoding="utf-8"):
                self.errors.append(f"Framework 依赖 com.alibaba.cloud: {pom.relative_to(ROOT)}")
        self._forbid_artifact_prefix("synapse-security", "synapse-oauth2")
        self._forbid_dependency("synapse-messaging", "synapse-audit")
        audit = self.projects.get("synapse-audit")
        if audit and (SYNAPSE_GROUP, "synapse-messaging") not in audit.dependencies:
            self.errors.append("synapse-audit 应通过 synapse-messaging 复用消息投递")
        self._forbid_artifact_fragments("synapse-web-core", ("servlet", "reactor", "webflux"))
        self._forbid_artifact_fragments("synapse-core", ("synapse-web", "synapse-security", "synapse-cloud"))
        self._forbid_source_imports("synapse-web-core", ("jakarta.servlet", "reactor.", "org.springframework.web.reactive"))
        self._forbid_source_imports("synapse-core", ("com.indigo.synapse.web", "com.indigo.synapse.security"))

    def _verify_web_context_trust_boundaries(self) -> None:
        """通用 Web Adapter 只能建立不可信技术上下文，不能从 Header 恢复身份。"""
        forbidden_tokens = (
            "OperationContextPropagationKeys.ACTOR_",
            "OperationContextPropagationKeys.TENANT_ID",
            "OperationContextPropagationKeys.INITIATOR_",
            "OperationContextSnapshotCodec",
            "SecurityOperationContextAdapter",
        )
        for artifact in ("synapse-webmvc", "synapse-webflux"):
            project = self.projects.get(artifact)
            if not project:
                continue
            for source in project.path.glob("src/main/**/*.java"):
                text = source.read_text(encoding="utf-8")
                if any(token in text for token in forbidden_tokens):
                    self.errors.append(
                        f"{artifact} 不得从普通请求建立认证身份: {source.relative_to(ROOT)}"
                    )

    def _verify_aop_infrastructure_boundaries(self) -> None:
        """Framework 模块只能贡献 Advisor，不得自行创建全局代理基础设施。"""
        forbidden_package = "org.springframework.aop.framework.autoproxy."
        for project in self.projects.values():
            for source in project.path.glob("src/main/**/*.java"):
                if forbidden_package in source.read_text(encoding="utf-8"):
                    self.errors.append(
                        f"Framework 生产代码不得注册或依赖 AutoProxyCreator: {source.relative_to(ROOT)}"
                    )

    def _forbid_dependency(self, artifact: str, forbidden: str) -> None:
        project = self.projects.get(artifact)
        if project and any(dep_artifact == forbidden for _, dep_artifact in project.dependencies):
            self.errors.append(f"{artifact} 禁止依赖 {forbidden}")

    def _forbid_artifact_prefix(self, artifact: str, prefix: str) -> None:
        project = self.projects.get(artifact)
        if project and any(dep_artifact.startswith(prefix) for _, dep_artifact in project.dependencies):
            self.errors.append(f"{artifact} 禁止依赖 {prefix}*")

    def _forbid_artifact_fragments(self, artifact: str, fragments: tuple[str, ...]) -> None:
        project = self.projects.get(artifact)
        if not project:
            return
        for _, dependency in project.dependencies:
            if any(fragment in dependency for fragment in fragments):
                self.errors.append(f"{artifact} 依赖边界违规: {dependency}")

    def _forbid_source_imports(self, artifact: str, prefixes: tuple[str, ...]) -> None:
        project = self.projects.get(artifact)
        if not project:
            return
        for source in project.path.glob("src/main/**/*.java"):
            text = source.read_text(encoding="utf-8")
            if any(f"import {prefix}" in text for prefix in prefixes):
                self.errors.append(f"{artifact} 源码边界违规: {source.relative_to(ROOT)}")

    def _required_text(self, node: ET.Element, query: str, pom: Path) -> str:
        value = self._text(node, query)
        if not value:
            self.errors.append(f"POM 缺少字段 {query}: {pom.relative_to(ROOT)}")
            return f"<missing:{pom.parent.name}>"
        return value

    @staticmethod
    def _text(node: ET.Element, query: str) -> str | None:
        child = node.find(query, NS)
        return child.text.strip() if child is not None and child.text else None

    def _compare_sets(self, message: str, values: set[str]) -> None:
        for value in sorted(values):
            self.errors.append(f"{message}: {value}")


def main() -> int:
    verifier = ArchitectureVerifier()
    verifier.verify()
    if verifier.errors:
        print("Architecture verification failed:", file=sys.stderr)
        for error in verifier.errors:
            print(f"  - {error}", file=sys.stderr)
        return 1
    jar_count = sum(project.packaging == "jar" for project in verifier.projects.values())
    print(f"Architecture verification passed: {len(verifier.projects)} reactor projects, {jar_count} published JARs.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
