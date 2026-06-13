package com.indigo.synapse.datapermission.resolver;

import com.indigo.synapse.datapermission.model.DataPermissionPolicy;

public interface DataPermissionResolver {

    DataPermissionPolicy resolve(String subjectId, String permissionKey);
}
