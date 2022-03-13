package tech.sergeyev.compmechlabauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ActiveDirectoryLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveDirectoryLdapAuthoritiesPopulator.class);

    @Override
    public Collection<? extends GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {
        LOGGER.info("getGrantedAuthorities() method");

        String[] groups = userData.getStringAttributes("memberOf");
        LOGGER.info("groups[]:{}", Arrays.toString(groups));

        if (groups == null) {
            return AuthorityUtils.NO_AUTHORITIES;
        }

        ArrayList<GrantedAuthority> authorities = new ArrayList<>(groups.length);
        LOGGER.info("authorities:{}", authorities);

        for (String group : groups) {
            authorities.add(new SimpleGrantedAuthority(
                    new DistinguishedName(group)
                            .removeLast().getValue())
            );
        }

        LOGGER.info("authorities now:{}", authorities);

        return authorities;
    }
}
