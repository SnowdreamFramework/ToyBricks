# ToyBricks
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.snowdream/toybricks/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.snowdream/toybricks)
[![GITHUB](https://img.shields.io/github/issues/badges/ToyBricks.svg)](https://github.com/SnowdreamFramework/ToyBricks/issues)
[![LICENSE](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

## Introduction

Android Library that provide simpler way to achieve modularity.

## Compile System requirements
JDK 7+

## Download
For Java project add in build.gradle file:

```groovy
dependencies {
    compile 'com.github.snowdream.toybricks:annotation:(latest version)'
    annotationProcessor 'com.github.snowdream.toybricks:processor:(latest version)'
    compile 'com.github.snowdream:toybricks:(latest version)'
}
```

For Kotlin project add in build.gradle file:
```groovy
kapt {
    generateStubs = true
}

dependencies {
    compile 'com.github.snowdream.toybricks:annotation:(latest version)'
    kapt 'com.github.snowdream.toybricks:processor:(latest version)'
    compile 'com.github.snowdream:toybricks:(latest version)'
}
```

## Advantage
1. Support for Java and Kotlin
1. APT: Compile-Time Annotation Processing with Java
1. No need to configure proguard yourself 
1. Smart checking for @Interface and @Implementation

## Disadvantage
1. Singleton class for kotlin start with object is not supported.

## Usage
For interface, annotate it with @Interface
```java
@Interface
public interface IText {

    String getText();
}
```

For default implementation, annotate it with @Implementation, and implements the defined Interface. 
```java
@Implementation(IText.class/*,singleton = true*/)
public class NewTextImpl implements IText {
    @Override
    public String getText() {
        return "NewTextImpl Implementation from "+ getClass().getCanonicalName();
    }
}
```

For global implementation, annotate it with @Implementation, and implements the defined Interface. 
```java
@Implementation(value = IText.class,global = true/*,singleton = true*/)
public class NewTextGobalImpl implements IText {
    @Override
    public String getText() {
        return "NewTextImpl Implementation from "+ getClass().getCanonicalName() ;
    }
}
```

Now, You can get the implementation for the interface as follows:
```java
IText text = ToyBricks.getImplementation(IText.class);
```

## Attention
1. For one interface, You can have only one default implementation and only one global implementation at the same time.
1. You can add `singleton = true` to define Singleton for java and kotlin
1. Global implementation has the higher priority than default implementation. so, if them exist at the same time ,the global implementation will be used.

## Suggestion
1. you should usually define the default implementation for one interface, not the global one.
1. when you want to realize a new implementation for the interface, you can define the new implementation as global.
1. when the global implementation is achieved and fully tested, delete the default implementation, and change the new global implementation to the default implementation.

## Contacts
* Email：yanghui1986527#gmail.com
* QQ Group: 529327615     
* WeChat Official Accounts:  sn0wdr1am    

![sn0wdr1am](https://static.dingtalk.com/media/lADOmAwFCs0BAs0BAg_258_258.jpg)

## License
```
Copyright (C) 2017 snowdream <yanghui1986527@gmail.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
