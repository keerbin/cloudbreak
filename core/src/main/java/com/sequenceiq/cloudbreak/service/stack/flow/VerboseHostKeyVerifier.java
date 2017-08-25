package com.sequenceiq.cloudbreak.service.stack.flow;

import java.security.PublicKey;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.schmizz.sshj.transport.verification.HostKeyVerifier;

public class VerboseHostKeyVerifier implements HostKeyVerifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerboseHostKeyVerifier.class);

    private Set<String> expectedFingerprints;

    public VerboseHostKeyVerifier(Set<String> expectedFingerprints) {
        this.expectedFingerprints = expectedFingerprints;
    }

    @Override
    public boolean verify(String hostname, int port, PublicKey key) {
        return true;
    }
}

