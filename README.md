# Lox Language
## Overview
Lox Language is a dynamically-typed interpreted programming language, inspired by various scripting languages such as Ruby and Python. This repository contains a Java implementation of the Lox interpreter, along with the necessary tools and examples.

## Installation
- Clone the repository: `git clone https://github.com/hazemkhaled98/Lox.git`
- change directory to Jlox: `cd Lox`
- Compile the source code: `javac jlox/Jlox.java`
- Run the Lox interpreter: `java jlox.Jlox`
- JDK 19 is recommended to run the interpreter  
## Usage
- To execute a Lox script, run the Lox interpreter followed by the path to the script file:  
`java jlox.Jlox [script path]`  
- Alternatively, you can launch the Lox interpreter in the interactive mode:  
   `java jlox.Jlox`  
  This will open a prompt where you can enter and execute Lox statements directly.

## features: 
- ### Dynamic Typing:
  ```
  var x = 5;       // x is dynamically assigned as an integer
  print(x);         // Output: 5

  x = "Hello";     // x is dynamically reassigned as a string
  print(x);         // Output: Hello

  x = true;        // x is dynamically reassigned as a boolean
  print(x);         // Output: true <\code>
  ```

- ### Data Types:
Number: Used to represent numerical values such as integers and floating-point numbers.  
```
var age = 25;
var price = 12.99;
```
- ### String:
Represents sequences of characters enclosed in double quotes.  
```
var message = "Hello, world!";
```
- ### Boolean:
Represents logical values, either true or false.
```
var isRaining = true;
```
- ### Nil:   
Represents the absence of a value.
```
var result = nil;
```
- ### Expressions:
Arithmetic expressions: Perform mathematical operations.
```
var sum = 5 + 3;
var product = 2 * 4;
```
- ### Comparison expressions:
Compare values and return a boolean result.
```
var isEqual = 10 == 10;
var isGreater = 5 > 2;
```
- ### Logical expressions:
Combine boolean values using logical operators.
```
var isTrue = true && false;
var isFalse = !true;
```
- ### Variable references:
Access and manipulate variables.
```
var x = 10;
var y = x + 5;
```
- ### Function calls:
Invoke functions and pass arguments.
```
print("Hello, world!");
calculateSum(3, 5);
```
- ### Statements:
Variable declaration: Create and initialize variables.
```
var name = "Alice";
var age = 30;
```
  - ### Control flow statements:
  Control the flow of execution based on conditions.
  ```
  if (age >= 18) {
    print("You are an adult.");
  } else {
    print("You are a minor.");
  }
```
  - ### Return statement: 
  Exit a function and return a value.
  ```
  fun calculateSum(a, b) {
    return a + b;
  }
  var result = calculateSum(3, 5);
  ```
  - ### Variables:
  Variables are used to store and manipulate data.
  They can hold values of various data types.
  Variables can be assigned new values and their values can be modified.
  ```
  var age = 25;
  age = age + 1;
  ```
  - ### Control Flow:
  Conditional statements: Execute different code blocks based on conditions.
  ```
  if (condition) {
    // Code executed if the condition is true
  } else {
    // Code executed if the condition is false
  }
  ```
  - ### Looping statements:
  Repeat code blocks based on conditions or fixed iterations.
  ```
  while (condition) {
    // Code executed repeatedly as long as the condition is true
  }
  
  for (var i = 0; i < 5; i = i + 1) {
    // Code executed 5 times, incrementing i by 1 in each iteration
  }
  ```
  - ### Functions:
  Functions are reusable blocks of code that perform specific tasks.
  They can accept parameters and return values.
  ```
  fun greet(name) {
    print("Hello, " + name + "!");
  }
  
  greet("Alice");
  
  fun add(a, b) {
    return a + b;
  }
  
  var sum = add(3, 5);
  ```
- ### Object-Oriented Programming Features:
Lox supports object-oriented programming concepts, allowing you to create classes, define methods, and work with objects. Here are the key OOP features in Lox 
- ### Classes:
In Lox, classes are used to define blueprints for creating objects.
You can define classes using the class keyword followed by the class name.
```
class Person {
  init(name) {
    this.name = name;
  }

  sayHello() {
    print("Hello, my name is " + this.name);
  }
}

var person = Person("John");
person.sayHello(); // Output: Hello, my name is John
person.age = 26; // adding age property to this instance
```
- ### Inheritance:
Lox supports single inheritance, where a class can inherit properties and methods from a parent class.
Inheritance allows you to create specialized classes that inherit and extend the functionality of their parent class.
```
class Student < Person {
  init(name, studentId) {
    super.init(name);
    this.studentId = studentId;
  }

  displayStudentInfo() {
    print("Student ID: " + this.studentId);
  }
}

var student = Student("Alice", 12345);
student.sayHello(); // Output: Hello, my name is Alice
student.displayStudentInfo(); // Output: Student ID: 12345
```   
- ### The Standard Library:
The Standard Library provides pre-defined functions to extend the functionality of the language.
```
print(5); // output: 5
println(5); // output: 5 and creates a new line
clock(); // returns the number of seconds that have passed since some fixed point in time. 
```

## Acknowledgments
This implementation of Lox Language is based on the book "Crafting Interpreters" by Robert Nystrom. Thanks, Robert for providing such a comprehensive resource for building interpreters and inspiring this project. check the book: https://craftinginterpreters.com/the-lox-language.html#top
