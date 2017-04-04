# Introdution

There is a common view, that the **_DTO (Data Transfer Object)_**  is an anti-pattern, but **_CSMapper_** targets to dispel this prejudice.

If we imagine a standard **_JavaEE_** application, then, as a rule, it has a "Layered architecture" with an API on top (**_Presensation Layer_**) and JPA domain model at the bottom (**_Persistence Layer_**). Also we can say that API parameters depends on **_JPA Entity Model_** at most, and is a kind of its projection.


# Usage

## Common Use Cases
