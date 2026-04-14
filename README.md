# Simple Interpreter

This project is a custom interpreter for a simple dynamically-typed programming language, written in Java.
It reads source code from stdin, runs it through a Lexer and Parser into an AST
and evaluates it using modern Java 21 features (Sealed Interfaces and Pattern Matching).
The final state of all variables is printed to stdout.

## Requirements
- Java 21 or higher
- Maven 3.8.7 or higher

## Build and Run

 ```shell
mvn clean package

mvn exec:java
```

Type your program line by line, then press `Ctrl+D` to run it.

## Features
- Variables, basic math, conditionals, loops, functions with return, multiple statements

## Examples

 ```shell
  stdin:
    x = 2
    y = (x + 2) * 2

  stdout:
    x: 2
    y: 8
```
```shell
  stdin:
    x = 20
    if x > 10 then y = 100 else y = 0

  stdout:
    x: 20
    y: 100
```
```shell
  stdin:
    x = 0
    y = 0
    while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1

  stdout:
    x: 3
    y: 11
```
```shell
  stdin:
    fun add(a, b) { return a + b }
    four = add(2, 2)

  stdout:
    four: 4
```
```shell
  stdin:
    fun fact_rec(n) { if n <= 0 then return 1 else return n*fact_rec(n-1) }
    a = fact_rec(5)

  stdout:
    a: 120
```
```shell
  stdin:
    fun fact_iter(n) { r = 1, while true do if n == 0 then return r else r = r * n, n = n - 1 }
    b = fact_iter(5)

  stdout:
    b: 120
```