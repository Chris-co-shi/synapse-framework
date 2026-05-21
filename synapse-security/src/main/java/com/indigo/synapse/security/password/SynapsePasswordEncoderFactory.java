package com.indigo.synapse.security.password;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class SynapsePasswordEncoderFactory {

    private SynapsePasswordEncoderFactory() {
    }

    public static PasswordEncoder bcrypt() {
        return new BCryptPasswordEncoder();
    }
}
