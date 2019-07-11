package com.billow.system.config.jpa;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class InjectOpreator implements AuditorAware<String> {

    /**
     * 给Bean中的 @CreatedBy  @LastModifiedBy 注入操作人
     *
     * @return
     */
    @Override
    public String getCurrentAuditor() {

        SecurityContext ctx = SecurityContextHolder.getContext();
        if (ctx == null) {
            return null;
        }
        if (ctx.getAuthentication() == null) {
            return null;
        } else {
            String loginUserName = ctx.getAuthentication().getName();
            return loginUserName;
        }
    }
}