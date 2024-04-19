## Highlights

1. Clear Naming: The names of the constants, methods and classes are clear and descriptive, making it easy to 
understand their purpose.
2. Use of Constants: Constants are used appropriately to store values that are unlikely to change during runtime, 
promoting maintainability and readability.
3. Comments: A brief comment is provided at the top of each class and method explaining its purpose, which is good for 
documentation.
4. REST Endpoint Design: The class DecisionEngineController defines a REST controller for handling loan decision requests. 
The endpoint is well-documented with comments explaining its purpose and behavior.
5. Exception Handling: The code includes exception handling for different scenarios, such as invalid input, 
no valid loan found, and unexpected errors, providing appropriate responses for each case.
6. Dependency Injection: Dependencies (DecisionEngine and DecisionResponse) are injected via constructor injection, 
promoting loose coupling and testability.
7. Response Entity Usage: ResponseEntity is used to provide different HTTP status codes and response bodies based on 
the outcome of the loan decision process.
8. Use of Lombok: The classes use Lombok annotations to automatically generate constructors, getters etc., reducing 
boilerplate code and improving readability.
9. Component Annotations: The @Component annotation indicates that this class is a Spring component and can be 
automatically detected and instantiated by the Spring framework.
10. Exception classes constructors: The class provides overloaded constructors to allow flexibility in creating 
instances of the exception with or without a cause (which can be useful for debugging purposes).
11. Separation of Concerns: The DecisionEngine class is focused on the responsibility of calculating the approved loan 
amount based on the provided inputs. It delegates the validation of inputs to a separate method verifyInputs(), 
promoting the Single Responsibility Principle (SRP).
12. The code is well stored and organized.

## Places for improvement

- DecisionEngineController
  1. Code Duplication: There's some code duplication in handling exceptions. Consider refactoring to eliminate 
  repetitive code and improve maintainability.
  2. Single Responsibility Principle (SRP): The requestDecision method seems to be doing multiple things - handling 
  exceptions, invoking decisionEngine, and setting the response. It could be beneficial to refactor this method into 
  smaller, more focused methods, each responsible for a single task.
  3. Error Message Clarity: While error messages are provided, they could be more descriptive and informative, 
  especially for unexpected errors. Providing additional context or logging the stack trace could assist in debugging.
  4. Open/Closed Principle (OCP): The requestDecision method can handle different types of exceptions, but it's not 
  easily extendable for new exception types. It's closed for modification in terms of adding new functionality but not 
  entirely open for extension. To adhere more closely to the OCP, consider using a strategy pattern or a more flexible 
  exception handling mechanism.
- DecisionEngine
  1. While the overall structure of the class adheres to SRP, the calculateApprovedLoan() method could be further 
  decomposed into smaller, more focused methods, each responsible for a specific part of the calculation process. This 
  would enhance readability and maintainability.
  2. Input Validation: While the verifyInputs() method validates the inputs for personal code, loan amount, and loan 
  period, it's recommended to provide more detailed error messages specifying the reason for validation failure. For 
  example, indicating the valid range for loan amount and loan period.
  3. Magic Numbers: There are still some magic numbers present in the code, such as 2500, 5000, 7500. It's better to 
  extract these as constants or configure them through properties to enhance readability and maintainability.
  4. highestValidLoanAmount method has unfinished JavaDoc (missing parameter information);

## The most important shortcoming

- Credit Score Calculation: The credit score calculation is not implemented in the service (method 
calculateApprovedLoan) according to the specified *credit score = (credit modifier / loan amount) * loan period*
requirement. 
- Finding suitable loan: The code in calculateApprovedLoan method doesn't consider the requested loan amount provided 
by the user. Instead, it seems to calculate the approved loan based solely on the loan period. Moreover, 
if a loan amount is not valid within any period, it throws an error about no suitable loans being found. **It shouldn't 
work like this according to the requirements**. If a person applies for €4000 and we would not approve it then we want 
to return the largest sum which we would approve, for example €2500. And if a suitable loan amount is not found within 
the selected period, **ONLY THEN** the decision engine should also try to find a new suitable period. Not vice versa 
and not without considering the change of the loan amount.

## The fixed version of the "most important shortcoming"

- The changes introduce another approach to determining the loan eligibility based on the credit score + adjusting 
firstly the loan amount until reaching the limits, and only then the loan period until a satisfactory credit score 
is achieved or until reaching the maximum loan period limits too.

## Problems with frontend

Here, I've encountered a major and a smaller problems:

- Major problem: According to the task, the decision engine is supposed to determine the maximum sum regardless of the 
requested loan amount by the person. For instance, if a person applies for €4000, but we determine that we would 
approve a larger sum, then the result should be the maximum sum we would approve. I've noticed that this feature is 
implemented correctly in the backend (the tests pass), but when I attempt to debug it in the frontend, the frontend 
only approves the sum the person has chosen, not the maximum available.
- Smaller problem: If we look at the left side of the loan period slider, we may notice that functionally it takes 12 
months as the minimum, but visually underneath is written 6. 
