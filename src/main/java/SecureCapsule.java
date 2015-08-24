/*
 * Copyright (c) 2015, Parallel Universe Software Co. and Contributors. All rights reserved.
 * 
 * This program and the accompanying materials are licensed under the terms 
 * of the Eclipse Public License v1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

import java.io.FilePermission;
import java.net.SocketPermission;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AllPermission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PropertyPermission;

/**
 *
 * @author pron
 */
public class SecureCapsule extends Capsule {
    private static final String PROP_SECURITY_POLICY = "capsule.security.policy";
    private static final String PROP_JAVA_SECURITY_POLICY = "java.security.policy";
    private static final String PROP_JAVA_SECURITY_MANAGER = "java.security.manager";

    private static final String ENV_CAPSULE_REPOS = "CAPSULE_REPOS";
    private static final String ENV_CAPSULE_LOCAL_REPO = "CAPSULE_LOCAL_REPO";

    private final Path jarFile;

    public SecureCapsule(Path jarFile) {
        super(jarFile);
        initSecurity();
        this.jarFile = getJarFile();  // save for when the wrapped capsule is loaded
    }

    public SecureCapsule(Capsule pred) {
        super(pred);
        initSecurity();
        this.jarFile = getJarFile(); // save for when the wrapped capsule is loaded
    }

    private void initSecurity() {
        Policy.setPolicy(new SandboxSecurityPolicy());
        System.setSecurityManager(new SecurityManager());
    }

    @Override
    protected Capsule loadTargetCapsule(ClassLoader parent, Path jarFile) {
        return super.loadTargetCapsule(new CapletLoader(parent), jarFile);
    }

    private class SandboxSecurityPolicy extends Policy {
        @Override
        public PermissionCollection getPermissions(ProtectionDomain domain) {
            return isWrapped(domain) ? pluginPermissions() : applicationPermissions();
        }

        private boolean isWrapped(ProtectionDomain domain) {
            return domain.getClassLoader() instanceof CapletLoader; // the wrapped capsule doesn't have permission to create new CLs or even access its CL's parent
        }

        private PermissionCollection pluginPermissions() {
            final Permissions permissions = new Permissions();

            permissions.add(new PropertyPermission("*", "read"));
            permissions.add(new FilePermission(getAppDir().toString(), "read"));
            //permissions.add(new RuntimePermission("shutdownHooks"));
            // Maven capsule:
            permissions.add(new RuntimePermission("getenv." + ENV_CAPSULE_REPOS));
            permissions.add(new RuntimePermission("getenv." + ENV_CAPSULE_LOCAL_REPO));
            permissions.add(new SocketPermission("https://repo1.maven.org/", "connect"));

            // absolutely no class loader permissions

            return permissions;
        }

        private PermissionCollection applicationPermissions() {
            final Permissions permissions = new Permissions();
            permissions.add(new AllPermission());
            return permissions;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T attribute(Map.Entry<String, T> attr) {
        if (ATTR_SYSTEM_PROPERTIES == attr) {
            final Map<String, String> props = new HashMap<>(super.attribute(ATTR_SYSTEM_PROPERTIES));

            props.put(PROP_JAVA_SECURITY_MANAGER, "");
            props.put(PROP_JAVA_SECURITY_POLICY, getPolicyFile());
            return (T) props;
        }

        if (ATTR_SECURITY_POLICY == attr) {
            return (T) getPolicyFile();
        }

        return super.attribute(attr);
    }

    private String getPolicyFile() {
        return getProperty(PROP_SECURITY_POLICY) != null ? getProperty(PROP_SECURITY_POLICY) : toJarUrl("security.policy");
    }

    @Override
    protected ProcessBuilder prelaunch(List<String> jvmArgs, List<String> args) {
        final ProcessBuilder pb = super.prelaunch(jvmArgs, args);

        final Path exec = Paths.get(pb.command().get(0));
        verify(!(exec.startsWith(appDir()) || exec.startsWith(getWritableAppCache())), "Local command: " + exec); // image must be outside writable area

        return pb;
    }

    private void verify(boolean cond, String message) {
        if (!cond)
            throw new SecurityException(message);
    }

    private static class CapletLoader extends ClassLoader {
        public CapletLoader(ClassLoader parent) {
            super(parent);
        }
    }

    private String toJarUrl(String relPath) {
        return "jar:file:" + jarFile.toAbsolutePath() + "!/" + relPath;
    }
}
