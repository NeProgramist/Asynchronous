# AsynKtronous Programming

Implementation of [Asynchronous Programming](https://github.com/HowProgrammingWorks/Index/blob/master/Courses/Asynchronous.md) patterns and principles in [Kotlin](https://github.com/JetBrains/kotlin) using [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines).

- [Timers, timeouts and EventEmitter](https://youtu.be/LK2jveAnRNg):
    - [x] [Timers](src/main/kotlin/Timers);
    - [x] [EventEmitters](src/main/kotlin/EventEmitter);
- [Asynchronous Programming with callbacks](https://youtu.be/z8Hg6zgi3yQ):
    - [x] [AsynchronousProgramming](src/main/kotlin/AsynchronousProgramming);
- [NonBlocking Asynchronous Iteration](https://youtu.be/wYA2cIRYLoA):
    - [x] [NonBlocking](src/main/kotlin/NonBlocking);
- [Asynchronous Programming with Promises](https://youtu.be/RMl4r6s1Y8M):
    - [x] [Promise](src/main/kotlin/Promise);
- [Asynchronous adapters: promisify, callbackify, asyncify](https://youtu.be/76k6_YkYRmU):
    - [x] [AsyncAdapter](src/main/kotlin/AsyncAdapter);
- [Asynchronous Data Collectors](https://youtu.be/tgodt1JL6II):
    - [x] [Collector](src/main/kotlin/Collector);
- [Thenable](https://youtu.be/DXp__1VNIvI):
    - [x] [Thenable](src/main/kotlin/Promise/a-thenable.kt) (implemented partially);
    - [ ] Implement all [repo](https://github.com/HowProgrammingWorks/Thenable);
- [Concurrent Queue](https://youtu.be/Lg46AH8wFvg);
    - [ ] ConcurrentQueue;
- [Revealing Constructor](https://youtu.be/leR5sXRkuJI):
    - [x] [RevealingConstructor](src/main/kotlin/RevealingConstructor);
    - [x] Used in [Collector/3-class.kt](src/main/kotlin/Collector/3-class.kt);
- [Futures](https://youtu.be/22ONv3AGXdk):
    - [ ] Future;
- [Deferred](https://youtu.be/a2fVA1o-ovM):
    - [ ] Deferred;
    - [x] Used as part of [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) instead of Promises in most cases;
- [Actor Model](https://youtu.be/xp5MVKEqxY4):
    - [ ] ActorModel;
- [Observer and Observable](https://youtu.be/_bFXuLcXoXg):
    - [ ] Observer;
- [Asynchronous Function Composition](https://youtu.be/3ZCrMlMpOrM):
    - [ ] AsyncCompose.

Note that due to such features as strong typing, JVM multithreading, etc, we didn't implement some examples as is but modified it according to Kotlin programming traditions and practices.
