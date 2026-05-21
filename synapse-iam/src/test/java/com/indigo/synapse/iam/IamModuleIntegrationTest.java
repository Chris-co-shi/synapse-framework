package com.indigo.synapse.iam;

import com.indigo.synapse.iam.application.command.LoginCommand;
import com.indigo.synapse.iam.infrastructure.persistence.entity.IamClientEntity;
import com.indigo.synapse.iam.infrastructure.persistence.entity.IamPermissionEntity;
import com.indigo.synapse.iam.infrastructure.persistence.entity.IamRoleEntity;
import com.indigo.synapse.iam.infrastructure.persistence.entity.IamRolePermissionEntity;
import com.indigo.synapse.iam.infrastructure.persistence.entity.IamUserEntity;
import com.indigo.synapse.iam.infrastructure.persistence.entity.IamUserRoleEntity;
import com.indigo.synapse.iam.infrastructure.persistence.mapper.IamClientMapper;
import com.indigo.synapse.iam.infrastructure.persistence.mapper.IamPermissionMapper;
import com.indigo.synapse.iam.infrastructure.persistence.mapper.IamRoleMapper;
import com.indigo.synapse.iam.infrastructure.persistence.mapper.IamRolePermissionMapper;
import com.indigo.synapse.iam.infrastructure.persistence.mapper.IamUserMapper;
import com.indigo.synapse.iam.infrastructure.persistence.mapper.IamUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = IamTestApplication.class,
        properties = {
                "spring.main.web-application-type=servlet",
                "synapse.security.key-id=kid-test",
                "synapse.security.issuer=http://localhost:18080"
        }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class IamModuleIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    IamUserMapper iamUserMapper;

    @Autowired
    IamRoleMapper iamRoleMapper;

    @Autowired
    IamPermissionMapper iamPermissionMapper;

    @Autowired
    IamUserRoleMapper iamUserRoleMapper;

    @Autowired
    IamRolePermissionMapper iamRolePermissionMapper;

    @Autowired
    IamClientMapper iamClientMapper;

    @BeforeEach
    void setUp() {
        seedCoreData();
    }

    @Test
    void shouldCreateUserThroughApiAndPersistToDatabase() throws Exception {
        mockMvc.perform(post("/api/admin/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"alice",
                                  "displayName":"Alice",
                                  "password":"secret",
                                  "roleCodes":["admin"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.data.username").value("alice"));

        assertThat(iamUserMapper.selectList(null)).hasSize(1);
        assertThat(iamUserRoleMapper.selectList(null)).hasSize(1);
    }

    @Test
    void shouldLoginAfterUserPersisted() throws Exception {
        mockMvc.perform(post("/api/admin/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"alice",
                                  "displayName":"Alice",
                                  "password":"secret",
                                  "roleCodes":["admin"]
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId":"admin-console",
                                  "username":"alice",
                                  "password":"secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.data.username").value("alice"))
                .andExpect(jsonPath("$.data.roles[0]").value("admin"));
    }

    private void seedCoreData() {
        if (iamClientMapper.selectList(null).isEmpty()) {
            IamClientEntity client = new IamClientEntity();
            client.setId("client-1");
            client.setClientId("admin-console");
            client.setEnabled(true);
            iamClientMapper.insert(client);
        }
        if (iamRoleMapper.selectList(null).isEmpty()) {
            IamRoleEntity role = new IamRoleEntity();
            role.setId("role-1");
            role.setCode("admin");
            role.setName("管理员");
            role.setEnabled(true);
            iamRoleMapper.insert(role);
        }
        if (iamPermissionMapper.selectList(null).isEmpty()) {
            IamPermissionEntity permission = new IamPermissionEntity();
            permission.setId("perm-1");
            permission.setCode("system:user:list");
            permission.setName("用户列表");
            permission.setEnabled(true);
            iamPermissionMapper.insert(permission);
        }
        if (iamRolePermissionMapper.selectList(null).isEmpty()) {
            IamRolePermissionEntity entity = new IamRolePermissionEntity();
            entity.setId("rp-1");
            entity.setRoleId("role-1");
            entity.setPermissionId("perm-1");
            iamRolePermissionMapper.insert(entity);
        }
    }

}
