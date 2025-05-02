# fortify-client

This library is hosted on Azure DevOps Artifacts and can be used in your project by adding the following to your `build.gradle` file:

```gradle
repositories {
  maven {
    url = uri('https://pkgs.dev.azure.com/hmcts/Artifacts/_packaging/hmcts-lib/maven/v1')
  }
}
dependencies {
  implementation 'com.github.hmcts:fortify-client:LATEST_TAG'
}
```