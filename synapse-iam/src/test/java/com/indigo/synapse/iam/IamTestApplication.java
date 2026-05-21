package com.indigo.synapse.iam;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = "com.indigo.synapse.iam")
@MapperScan("com.indigo.synapse.iam.infrastructure.persistence.mapper")
public class IamTestApplication {
}
