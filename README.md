# Rule_Engine



The project is a **Spring-based Rule Engine** designed to dynamically define, store, and evaluate logical rules, using an Abstract Syntax Tree (AST) structure to parse and apply complex conditions based on user-defined criteria. The rule engine provides flexible support for creating, combining, and evaluating logical expressions involving comparison, string, and logical operators.

### Key Features

1. **Rule Creation**:
   - The `RuleService` class allows users to create rules as string expressions. Each rule is parsed into tokens, validated for proper syntax, and converted into an AST representation before being saved in a database.
   - Operators supported include comparison operators (`>`, `<`, `>=`, `<=`, `=`, `!=`), logical operators (`AND`, `OR`), and string operators (`=`, `!=`).
   - Custom validation ensures that each rule follows correct syntax, such as balanced parentheses and valid operators.

2. **AST-Based Rule Storage**:
   - Rules are parsed into an AST where each node represents an operand or operator. This structure makes the rule engine highly adaptable, as complex logical expressions can be represented and evaluated recursively.
   - The AST is stored in a database via a `NodeRepository`, making it retrievable for future evaluation or combination with other rules.

3. **Rule Combination**:
   - Users can combine multiple rules using a dominant logical operator (`AND` or `OR`) based on the most frequently occurring operator in the selected rules.
   - The combined AST represents a logical union of all input rules, allowing complex criteria to be created from simpler individual rules.

4. **Rule Evaluation**:
   - A rule can be evaluated against dynamic user data, verifying whether the data meets the defined criteria.
   - The `evaluateNode` method recursively evaluates each node in the AST. For operators, it evaluates left and right operands, and for operands, it retrieves values from the user data or defaults to numeric or string literals if specified.
   - The engine supports both numeric and string comparisons, type-checking operands to ensure compatible comparisons.

5. **Custom Error Handling**:
   - Detailed exception handling ensures that invalid input or syntax errors are caught early in rule creation and evaluation. The project also uses a `CombinationException` for specific errors related to rule combinations.
   - IllegalArgumentExceptions provide informative messages for unsupported operations, missing user data attributes, and incompatible operand types.

### Components and Technologies

- **Spring Framework**: Core framework used for dependency injection, service management, and data access.
- **Thymeleaf**: Used for frontend templates, allowing users to submit forms for rule creation, evaluation, and combination.
- **Spring Data JPA**: For database interactions to persist and retrieve the AST nodes of each rule.
- **JSON Support**: Uses JSON-based representations of ASTs, making it compatible with JSON-based frontend applications.

### How It Works

- Users can define rules through a frontend form, which is passed to the backend and validated. The rule is then tokenized and converted to an AST, stored in a database.
- Multiple stored rules can be combined based on a chosen operator, allowing flexible, hierarchical rule creation.
- Rules are evaluated by traversing the AST and comparing the specified user data attributes to the rule criteria, using logical and comparison operations to verify conditions.

This rule engine provides a powerful way to handle dynamic, user-defined business logic, making it ideal for applications requiring customizable conditions such as dynamic access control, custom workflows, or complex decision-making rules.
