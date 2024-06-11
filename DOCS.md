# Design Documentation & Idea Dump

## Vanilla Command Execution

Vanilla command execution is rather simple, but also a bit strange. The central object for command execution is
the `ExecutionContext`. All commands are compiled (ultimately) to a `UnboundEntryAction<T>`, which is the "instruction"
that the `ExecutionContext` runs.

The `ExecutionContext` class holds two queues:

1. Main execution queue `commandQueue`
2. Temporary queue `newTopCommands`

To schedule an "instruction" to be executed, `queueNext` is called. `queueNext` simply inserts the "instruction"
into `newTopCommands`.

During the main execution loop (in `execute`):

1. The first queued entry is popped
2. It is executed
3. All instructions queued with `queueNext` during step 2 is inserted in *front* of the execution queue (that
   is, `newTopCommands` is copied into the front of `commandQueue`).
    - Specifically, the *first* entry in `newTopCommands` will be the first entry of `commandQueue`. In code
    - Pseudocode: `reverse(newTopCommands).forEach(commandQueue::addFront)`

For `MCFunction`, what happens is that a "continuation task" is enqueued, which during execution, will simply queue the
next instruction and then itself. As an example:

```  
foo  
bar
```  

Would lead to the following execution steps:

1. The command function is called, which enqueues the continuation task
    - `commandQueue` = `[continuation_task(0, [foo, bar])]`
2. The first entry in `commandQueue` is popped (`continuation_task(0, [foo, bar])`) and executed.
    - `commandQueue` = `[]`
    - `newTopCommands` = `[foo, continuation_task(1, [foo, bar])]`
3. `newTopCommands` is copied into `commandQueue`, and `newTopCommands` is cleared.
    - `commandQueue` = `[foo, continuation_task(1, [foo, bar])]`
4. The first entry in `commandQueue` is popped (`foo`) and executed.
    - `commandQueue` = `[continuation_task(1, [foo, bar])]`
5. The first entry in `commandQueue` is popped (`continuation_task(1, [foo, bar])`) and executed.
    - `commandQueue` = `[]`
    - `newTopCommands` = `[bar]`
6. `newTopCommands` is copied into `commandQueue`, and `newTopCommands` is cleared.
    - `commandQueue` = `[bar]`
7. The first entry in `commandQueue` is popped (`bar`) and executed.
    - `commandQueue` = `[]`
8. Execution terminates

This is a relatively trivial example. A more interesting one would be a function call:

```
# a.mcfunction
foo
# b.mcfunction
function a
bar
```

TODO: walk through the steps.

## Control Flow Execution Architecture

`ContinuationTask` is limited since it does not allow for control flow (the instruction index is always incremented).
`FuncExecTask` solves this by allowing its entries to implement the `ControlInstr` interface, which enables commands to
modify the state. The function execution state now models a stack machine, with the following:

1. An instruction pointer
2. The current `CommandSourceStack`
3. An execution stack, capable of holding local and temporary variables

Control instructions may modify the state, thus achieving control flow. Since this system is modeled similarly to
existing computer/vm architecture (specifically, the JVM), higher level constructs (like `run`, `loop`, etc.) are mapped
to recognizable low-level constructs (such as conditional/static branches for loops, `call` and `ret` instructions for
subroutine), etc.

## MCFunction "compiler" design

The compiler design is relatively simple. A "lexer" phase splits `MCFunction` into lines, a "parser" phase converts the
list of lines into an AST, which is then used to run code generation. Note that the "semantic analysis" phase is fused
with the "codegen" phase, so checks like "is this a valid break" or "do I know this subroutine" happen during codegen,
and thus, error reporting should happen after codegen.

To control compiler features flags, settings, and other things, `pragma` statements may be used. For example, to enable
control flow v2 syntax, use `pragma enable cfv2`.

## Replacing Minecraft's `ExecutionContext`

It may be worthwhile to replace MC's `ExecutionContext` with a more direct VM approach. 