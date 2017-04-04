# Introdution

There is a common view, that the **_DTO (Data Transfer Object)_**  is an anti-pattern, but **_CSMapper_** targets to dispel this prejudice.

If we imagine a standard **_JavaEE_** application, then, as a rule, it has a "Layered architecture" with an API on top, which is **_Presensation Layer_**, and JPA domain model at the bottom, which is **_Persistence Layer_**. Also we can say that API parameters depends on **_JPA Entity Model_** at most, and is a kind of its projection. So here is the dummy copy paste come from, which is the reason that makes DTO called antipattern. But this copy paste problem can be very simply solved, which is what the **_CSMapper_** makes, and turn the DTO back into a very cool pattern.

**//TODO: compleate this section**

# Usage

## Common Use Cases
