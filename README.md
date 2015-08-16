# Capsule Docker

A caplet that sandboxes a [capsule](https://github.com/puniverse/capsule) through a Java security policies file determined at runtime by the `capsule.security.policy` property (the path is relative to the capsule JAR's root).

## Usage

    java -jar capsule-secure.jar your-capsule.jar

or embed this caplet in your capsule using the `Caplets` attribute (`SecureCapsule`).

## License

    MIT
