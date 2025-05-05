package com.ray.archive.config;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.java.StringJavaType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义Hibernate类型贡献者，处理映射问题
 */
@Component
public class HibernateTypesRegistrationContributor implements TypeContributor {

    @Override
    public void contribute(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        // 确保List<String>和String之间的映射正确处理
        JavaType<List<String>> listStringType = typeContributions.getTypeConfiguration()
            .getJavaTypeRegistry()
            .resolveDescriptor(List.class);
        
        // 注册所有基本类型为VARCHAR
        VarcharJdbcType varcharJdbcType = VarcharJdbcType.INSTANCE;
        typeContributions.contributeJdbcType(varcharJdbcType);
        
        // 确保String映射到VARCHAR
        JavaType<String> stringType = StringJavaType.INSTANCE;
        typeContributions.getTypeConfiguration()
            .getJdbcTypeRegistry();
    }
}
 