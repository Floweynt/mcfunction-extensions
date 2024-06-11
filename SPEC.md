# MCFunction Extension Specification

## Base

By default, the following statements are added:

### Pragma

Usage:

- `pragma enable <feat>`: Enables a feature. Allowed values are:
    - `cfv2`: Enable "Control Flow V2" extension (disables Control Flow V1).
    - `subroutine`: Enables "Subroutine" extensions.
    - `debug_dump`: Enables debug dump, which logs AST and codegen to console (only useful for development/debugging of
      the compiler itself).
- `pragma enable <feat>`: Disables a feature. Allowed values are:
    - `cfv2`: Disables "Control Flow V2" extension (enables Control Flow V1).
    - `subroutine`: Disables "Subroutine" extensions.
    - `debug_dump`: Disables debug dump.

Description:

Controls various compiler features and characteristics.

### Return

Usage: `return`

Description:

Returns from the current MCFunction, without an exit code. Use vanilla's version of `return` to return a non-void value.

## Extension: Control Flow V1

This extension is enabled by default, and is mutually exclusive with "Control Flow V2."

### Run

Usage:

```
run <execute_args> { 
  ... body ...
}
```

Description:

Equivalent to running a vanilla `execute <execute_args> run body_function`, where `body_function` is a MCFunction that
contains the body.

### Loop

Usage:

```
run <execute_args> { 
  ... body ...
}
```

Description:

Run, but recursive!

## Extension: Control Flow V2