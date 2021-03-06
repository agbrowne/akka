# recoverWith

Allow switching to alternative Source when a failure has happened upstream.

@ref[Simple operators](../index.md#simple-operators)

@@@div { .group-scala }

## Signature

@@signature [Flow.scala]($akka$/akka-stream/src/main/scala/akka/stream/scaladsl/Flow.scala) { #recoverWith }

@@@

## Description

Allow switching to alternative Source when a failure has happened upstream.

Throwing an exception inside `recoverWith` _will_ be logged on ERROR level automatically.


@@@div { .callout }

**emits** the element is available from the upstream or upstream is failed and pf returns alternative Source

**backpressures** downstream backpressures, after failure happened it backprssures to alternative Source

**completes** upstream completes or upstream failed with exception pf can handle

@@@

