# PlainLang to MIXAL Compiler

A compiler that translates **PlainLang** — a simple, C-like programming language — into **MIXAL** assembly code for the MIX computer architecture. Built with Java, Maven, and ANTLR4.

## Overview

The compiler implements a full compilation pipeline:

```
PlainLang source (.plain)
        │
        ▼
   Lexer & Parser       ← ANTLR4-generated from PlainLang.g4
        │
        ▼
  Abstract Syntax Tree  ← AstBuilder + AST node classes
        │
        ▼
  Semantic Analysis     ← Type checking, symbol/method table validation
        │
        ▼
  Code Generation       ← Outputs MIXAL assembly (.mix)
```

## Features

- Integer variables and arithmetic expressions (`+`, `-`, `*`, `/`)
- Comparison operators (`<`, `>`, `<=`, `>=`, `==`, `!=`)
- Control flow: `if/else`, `while`, `break`, `return`
- Method declarations with parameters and return values
- Recursive method calls
- Semantic error detection (type errors, undefined variables, syntax errors)

## Project Structure

```
├── src/
│   └── main/
│       ├── antlr4/compiler/parser/
│       │   └── PlainLang.g4          # ANTLR4 grammar definition
│       └── java/compiler/
│           ├── Main.java             # Entry point
│           ├── ast/                  # AST node classes
│           ├── codegen/
│           │   └── CodeGenerator.java
│           ├── semantics/
│           │   └── SemanticAnalyzer.java
│           └── symbols/              # Symbol & method tables
├── examples/                         # Sample programs
│   ├── program1.plain / .mix
│   ├── program2.plain / .mix
│   ├── program3.plain / .mix
│   ├── factorial.plain / .mix
│   ├── error1.plain                  # Syntax error example
│   └── error2.plain                  # Semantic error example
└── pom.xml
```

## Build & Run

**Requirements:** Java 11+, Maven 3.x

### Build
```bash
mvn clean package
```

### Run on all example files
Place the `.plain` files in the project root and run:
```bash
java -cp target/classes compiler.Main
```

### Run on a specific file
```bash
java -cp target/classes compiler.Main path/to/your_program.plain
```

The compiler will generate a `.mix` output file in the same directory.

## Example

**Input** (`factorial.plain`):
```
int factorial(int n) {
  if (n <= 1) {
    return 1;
  } else {
    return n * factorial(n - 1);
  }
}

int main() {
  return factorial(5);
}
```

**Output** (`factorial.mix`):
```
START   ORIG 1000
        JMP MAIN
FACTORIAL   NOP
        LDA FACTORIALN
        ...
```

## Error Detection

The compiler catches both syntax and semantic errors:

- `error1.plain` — syntax error (malformed expression)
- `error2.plain` — semantic error (type mismatch / undefined variable)

## Technologies

- **Java** — core implementation language
- **ANTLR4** — lexer and parser generation from grammar
- **Maven** — build tool (`mvn clean package`)
