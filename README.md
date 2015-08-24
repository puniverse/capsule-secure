# Capsule Secure

A [caplet](https://github.com/puniverse/capsule#what-are-caplets) that sandboxes a [capsule](https://github.com/puniverse/capsule) through a Java security policies resource determined at runtime by the `capsule.security.policy` property (the pathname is relative to the capsule JAR's root).

## Usage

The Gradle-style dependency you need to embed in your Capsule JAR, which you can generate with the tool you prefer (f.e. with plain Maven/Gradle as in [Photon](https://github.com/puniverse/photon) and [`capsule-gui-demo`](https://github.com/puniverse/capsule-gui-demo) or higher-level [Capsule build plugins](https://github.com/puniverse/capsule#build-tool-plugins)), is `co.paralleluniverse:capsule-secure:0.1.0`. Also include the caplet class in your Capsule manifest, for example:

``` gradle
    Caplets: MavenCapsule SecureCapsule
```

`capsule-secure` can also be run as a wrapper capsule without embedding it:

``` bash
$ java -Dcapsule.log=verbose -Dcapsule.security.policy=sec.policy -jar capsule-secure-0.1.0.jar my-capsule.jar my-capsule-arg1 ...
```

It can be both run against (or embedded in) plain (e.g. "fat") capsules and [Maven-based](https://github.com/puniverse/capsule-maven) ones.

## Security Notes

 * Some basic permissions enabling the usage of [`maven-capsule`](https://github.com/puniverse/capsule-maven) at present are always granted, specifically reading the `CAPSULE_REPOS` and `CAPSULE_LOCAL_REPO` environment variables as well as connecting to Maven Central (https://repo1.maven.org/).

## License

    MIT
