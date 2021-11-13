# A thread safe LiveData

[![](https://www.jitpack.io/v/dqh147258/SafeLiveData.svg)](https://www.jitpack.io/#dqh147258/SafeLiveData)

## Features
- you can use the LiveData in background thread safely
- you can use the LiveData out of ViewModel without considering memory leak(except observeForever)

## How to use

dependencies

```groovy
	allprojects {
		repositories {
			//...
			maven { url 'https://www.jitpack.io' }
		}
	}
```

```groovy
	dependencies {
            def lastSafeLiveDataVersion = "1.1.1" //replace lastSafeLiveDataVersion
	        implementation "com.github.dqh147258:SafeLiveData:$lastSafeLiveDataVersion"
	}
```

use it by 
```kotlin
val infoLiveData = SafeLiveData<Int>()
```

or 
```kotlin
    val yourLiveData = MediatorLiveData<Int>()
    val infoLiveData = SafeLiveData<Int>(yourLiveData)
```

