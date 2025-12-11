package com.guinetik.corefun;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Result}.
 *
 * Pattern notes:
 * - Use @DisplayName for human-readable test output
 * - Use @Nested to group related tests
 * - Follow "given_when_then" or "methodName_scenario_expectedBehavior" naming
 * - One assertion per test when practical (though multiple related assertions are fine)
 */
@DisplayName("Result")
class ResultTest {

    @Nested
    @DisplayName("Success")
    class SuccessTests {

        @Test
        @DisplayName("isSuccess returns true")
        void isSuccess_returnsTrue() {
            Result<String, String> result = Result.success("value");

            assertTrue(result.isSuccess());
            assertFalse(result.isFailure());
        }

        @Test
        @DisplayName("getSuccess returns the value")
        void getSuccess_returnsValue() {
            Result<String, String> result = Result.success("hello");

            assertEquals("hello", result.getSuccess());
        }

        @Test
        @DisplayName("getFailure throws on success")
        void getFailure_throwsOnSuccess() {
            Result<String, String> result = Result.success("value");

            assertThrows(IllegalStateException.class, result::getFailure);
        }

        @Test
        @DisplayName("getOrElse returns the success value")
        void getOrElse_returnsSuccessValue() {
            Result<String, String> result = Result.success("value");

            assertEquals("value", result.getOrElse("default"));
        }

        @Test
        @DisplayName("toString shows Success wrapper")
        void toString_showsSuccessWrapper() {
            Result<String, String> result = Result.success("test");

            assertEquals("Success[test]", result.toString());
        }

        @Test
        @DisplayName("peek executes action with value")
        void peek_executesWithValue() {
            AtomicReference<Object> captured = new AtomicReference<>();
            Result<String, String> result = Result.success("hello");

            Result<String, String> same = result.peek(captured::set);

            assertEquals("hello", captured.get());
            assertSame(result, same);
        }

        @Test
        @DisplayName("peekSuccess executes action with value")
        void peekSuccess_executesWithValue() {
            AtomicReference<String> captured = new AtomicReference<>();
            Result<String, String> result = Result.success("hello");

            Result<String, String> same = result.peekSuccess(captured::set);

            assertEquals("hello", captured.get());
            assertSame(result, same);
        }

        @Test
        @DisplayName("peekFailure does nothing on success")
        void peekFailure_doesNothingOnSuccess() {
            AtomicReference<String> captured = new AtomicReference<>();
            Result<String, String> result = Result.success("hello");

            Result<String, String> same = result.peekFailure(captured::set);

            assertNull(captured.get());
            assertSame(result, same);
        }

        @Test
        @DisplayName("getOrElseGet returns value without calling supplier")
        void getOrElseGet_returnsValue() {
            Result<String, String> result = Result.success("value");

            String value = result.getOrElseGet(() -> {
                fail("Supplier should not be called");
                return "default";
            });

            assertEquals("value", value);
        }

        @Test
        @DisplayName("mapFailure does nothing on success")
        void mapFailure_doesNothingOnSuccess() {
            Result<String, String> result = Result.success("value");

            Result<String, Integer> mapped = result.mapFailure(String::length);

            assertTrue(mapped.isSuccess());
            assertEquals("value", mapped.getSuccess());
        }
    }

    @Nested
    @DisplayName("Failure")
    class FailureTests {

        @Test
        @DisplayName("isFailure returns true")
        void isFailure_returnsTrue() {
            Result<String, String> result = Result.failure("error");

            assertTrue(result.isFailure());
            assertFalse(result.isSuccess());
        }

        @Test
        @DisplayName("getFailure returns the error")
        void getFailure_returnsError() {
            Result<String, String> result = Result.failure("oops");

            assertEquals("oops", result.getFailure());
        }

        @Test
        @DisplayName("getSuccess throws on failure")
        void getSuccess_throwsOnFailure() {
            Result<String, String> result = Result.failure("error");

            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                result::getSuccess
            );
            assertTrue(ex.getMessage().contains("error"));
        }

        @Test
        @DisplayName("getOrElse returns the default value")
        void getOrElse_returnsDefault() {
            Result<String, String> result = Result.failure("error");

            assertEquals("default", result.getOrElse("default"));
        }

        @Test
        @DisplayName("toString shows Failure wrapper")
        void toString_showsFailureWrapper() {
            Result<String, String> result = Result.failure("err");

            assertEquals("Failure[err]", result.toString());
        }

        @Test
        @DisplayName("getOrElseGet calls supplier on failure")
        void getOrElseGet_callsSupplier() {
            Result<String, String> result = Result.failure("error");

            String value = result.getOrElseGet(() -> "computed default");

            assertEquals("computed default", value);
        }

        @Test
        @DisplayName("mapFailure transforms error")
        void mapFailure_transformsError() {
            Result<String, String> result = Result.failure("error");

            Result<String, Integer> mapped = result.mapFailure(String::length);

            assertTrue(mapped.isFailure());
            assertEquals(5, mapped.getFailure());
        }

        @Test
        @DisplayName("peek executes action with error")
        void peek_executesWithError() {
            AtomicReference<Object> captured = new AtomicReference<>();
            Result<String, String> result = Result.failure("error");

            result.peek(captured::set);

            assertEquals("error", captured.get());
        }

        @Test
        @DisplayName("peekSuccess does nothing on failure")
        void peekSuccess_doesNothingOnFailure() {
            AtomicReference<String> captured = new AtomicReference<>();
            Result<String, String> result = Result.failure("error");

            Result<String, String> same = result.peekSuccess(captured::set);

            assertNull(captured.get());
            assertSame(result, same);
        }

        @Test
        @DisplayName("peekFailure executes action with error")
        void peekFailure_executesWithError() {
            AtomicReference<String> captured = new AtomicReference<>();
            Result<String, String> result = Result.failure("error");

            Result<String, String> same = result.peekFailure(captured::set);

            assertEquals("error", captured.get());
            assertSame(result, same);
        }
    }

    @Nested
    @DisplayName("map")
    class MapTests {

        @Test
        @DisplayName("transforms success value")
        void map_transformsSuccessValue() {
            Result<Integer, String> result = Result.success(5);

            Result<Integer, String> mapped = result.map(n -> n * 2);

            assertEquals(10, mapped.getSuccess());
        }

        @Test
        @DisplayName("propagates failure unchanged")
        void map_propagatesFailure() {
            Result<Integer, String> result = Result.failure("error");

            Result<Integer, String> mapped = result.map(n -> n * 2);

            assertTrue(mapped.isFailure());
            assertEquals("error", mapped.getFailure());
        }

        @Test
        @DisplayName("can change success type")
        void map_canChangeType() {
            Result<String, String> result = Result.success("hello");

            Result<Integer, String> mapped = result.map(String::length);

            assertEquals(5, mapped.getSuccess());
        }
    }

    @Nested
    @DisplayName("flatMap")
    class FlatMapTests {

        @Test
        @DisplayName("chains successful operations")
        void flatMap_chainsSuccessfulOperations() {
            Result<Integer, String> result = Result.success(10);

            Result<Integer, String> chained = result
                .flatMap(n -> Result.success(n + 5))
                .flatMap(n -> Result.success(n * 2));

            assertEquals(30, chained.getSuccess());
        }

        @Test
        @DisplayName("short-circuits on failure")
        void flatMap_shortCircuitsOnFailure() {
            Result<Integer, String> result = Result.success(10);

            Result<Integer, String> chained = result
                .flatMap(n -> Result.<Integer, String>failure("boom"))
                .flatMap(n -> Result.success(n * 2)); // never called

            assertTrue(chained.isFailure());
            assertEquals("boom", chained.getFailure());
        }

        @Test
        @DisplayName("propagates initial failure")
        void flatMap_propagatesInitialFailure() {
            Result<Integer, String> result = Result.failure("initial error");

            Result<Integer, String> chained = result
                .flatMap(n -> Result.success(n * 2));

            assertTrue(chained.isFailure());
            assertEquals("initial error", chained.getFailure());
        }
    }

    @Nested
    @DisplayName("fold")
    class FoldTests {

        @Test
        @DisplayName("applies success function on success")
        void fold_appliesSuccessFunction() {
            Result<Integer, String> result = Result.success(42);

            String folded = result.fold(
                error -> "Error: " + error,
                value -> "Value: " + value
            );

            assertEquals("Value: 42", folded);
        }

        @Test
        @DisplayName("applies failure function on failure")
        void fold_appliesFailureFunction() {
            Result<Integer, String> result = Result.failure("oops");

            String folded = result.fold(
                error -> "Error: " + error,
                value -> "Value: " + value
            );

            assertEquals("Error: oops", folded);
        }

        @Test
        @DisplayName("can return different type")
        void fold_canReturnDifferentType() {
            Result<String, String> result = Result.success("test");

            int length = result.fold(
                error -> -1,
                String::length
            );

            assertEquals(4, length);
        }
    }

    @Nested
    @DisplayName("validate")
    class ValidateTests {

        @Test
        @DisplayName("passes when predicate is true")
        void validate_passesWhenPredicateTrue() {
            Result<Integer, String> result = Result.success(25);

            Result<Integer, String> validated = result.validate(
                age -> age >= 18,
                age -> "Too young: " + age
            );

            assertTrue(validated.isSuccess());
            assertEquals(25, validated.getSuccess());
        }

        @Test
        @DisplayName("fails when predicate is false")
        void validate_failsWhenPredicateFalse() {
            Result<Integer, String> result = Result.success(15);

            Result<Integer, String> validated = result.validate(
                age -> age >= 18,
                age -> "Too young: " + age
            );

            assertTrue(validated.isFailure());
            assertEquals("Too young: 15", validated.getFailure());
        }

        @Test
        @DisplayName("skips validation on failure")
        void validate_skipsOnFailure() {
            Result<Integer, String> result = Result.failure("already failed");

            Result<Integer, String> validated = result.validate(
                age -> age >= 18,
                age -> "Too young: " + age
            );

            assertTrue(validated.isFailure());
            assertEquals("already failed", validated.getFailure());
        }
    }

    @Nested
    @DisplayName("recover")
    class RecoverTests {

        @Test
        @DisplayName("recovers from failure")
        void recover_recoversFromFailure() {
            Result<String, String> result = Result.failure("error");

            Result<String, String> recovered = result.recover(
                error -> Result.success("recovered")
            );

            assertTrue(recovered.isSuccess());
            assertEquals("recovered", recovered.getSuccess());
        }

        @Test
        @DisplayName("does nothing on success")
        void recover_doesNothingOnSuccess() {
            Result<String, String> result = Result.success("original");

            Result<String, String> recovered = result.recover(
                error -> Result.success("recovered")
            );

            assertTrue(recovered.isSuccess());
            assertEquals("original", recovered.getSuccess());
        }
    }

    @Nested
    @DisplayName("equality")
    class EqualityTests {

        @Test
        @DisplayName("equal successes are equal")
        void equalSuccesses_areEqual() {
            Result<String, String> a = Result.success("value");
            Result<String, String> b = Result.success("value");

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("different successes are not equal")
        void differentSuccesses_areNotEqual() {
            Result<String, String> a = Result.success("one");
            Result<String, String> b = Result.success("two");

            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("success and failure are not equal")
        void successAndFailure_areNotEqual() {
            Result<String, String> success = Result.success("value");
            Result<String, String> failure = Result.failure("value");

            assertNotEquals(success, failure);
        }

        @Test
        @DisplayName("equal failures are equal")
        void equalFailures_areEqual() {
            Result<String, String> a = Result.failure("error");
            Result<String, String> b = Result.failure("error");

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("different failures are not equal")
        void differentFailures_areNotEqual() {
            Result<String, String> a = Result.failure("one");
            Result<String, String> b = Result.failure("two");

            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("failure equals itself")
        void failure_equalsItself() {
            Result<String, String> a = Result.failure("error");

            assertEquals(a, a);
        }

        @Test
        @DisplayName("failure not equal to non-Result")
        void failure_notEqualToNonResult() {
            Result<String, String> a = Result.failure("error");

            assertNotEquals(a, "error");
            assertNotEquals(a, null);
        }

        @Test
        @DisplayName("failure with null error")
        void failure_withNullError() {
            Result<String, String> a = Result.failure(null);
            Result<String, String> b = Result.failure(null);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
            assertEquals(0, a.hashCode());
        }

        @Test
        @DisplayName("failure with null not equal to failure with value")
        void failure_nullNotEqualToValue() {
            Result<String, String> nullFailure = Result.failure(null);
            Result<String, String> valueFailure = Result.failure("error");

            assertNotEquals(nullFailure, valueFailure);
            assertNotEquals(valueFailure, nullFailure);
        }

        @Test
        @DisplayName("success with null value")
        void success_withNullValue() {
            Result<String, String> a = Result.success(null);
            Result<String, String> b = Result.success(null);

            assertEquals(a, b);
            assertEquals(0, a.hashCode());
        }
    }
}
