"""
Example application with a stateful vulnerability.

This simulates a password checking system that leaks state information
through a global progress variable - a classic waterfall vulnerability.
"""

# Global state - simulates session state
progress = 0
SECRET = "FUZZINGLABS"  # 11 characters


def check_secret(input_data: bytes) -> bool:
    """
    Vulnerable function: checks secret character by character.

    This is a waterfall vulnerability - state leaks through the progress variable.

    Real-world analogy:
    - Timing attacks on password checkers
    - Protocol state machines with sequential validation
    - Multi-step authentication flows

    Args:
        input_data: Input bytes to check

    Returns:
        True if progress was made, False otherwise

    Raises:
        SystemError: When complete secret is discovered (vulnerability trigger)
    """
    global progress

    if len(input_data) > progress:
        if input_data[progress] == ord(SECRET[progress]):
            progress += 1

            # Progress indicator (useful for monitoring during fuzzing)
            if progress % 2 == 0:  # Every 2 characters
                print(f"[DEBUG] Progress: {progress}/{len(SECRET)} characters matched")

            # VULNERABILITY: Crashes when complete secret found
            if progress == len(SECRET):
                raise SystemError(f"SECRET COMPROMISED: {SECRET}")

            return True
        else:
            # Wrong character - reset progress
            progress = 0
            return False

    return False


def reset_state():
    """Reset the global state (useful for testing)"""
    global progress
    progress = 0


if __name__ == "__main__":
    """Example usage showing the vulnerability"""
    print("=" * 60)
    print("Waterfall Vulnerability Demonstration")
    print("=" * 60)
    print(f"Secret: {SECRET}")
    print(f"Secret length: {len(SECRET)} characters")
    print()

    # Test inputs showing progressive discovery
    test_inputs = [
        b"F",           # First char correct
        b"FU",          # First two chars correct
        b"FUZ",         # First three chars correct
        b"WRONG",       # Wrong - resets progress
        b"FUZZINGLABS", # Complete secret - triggers crash!
    ]

    for test in test_inputs:
        reset_state()  # Start fresh for each test
        print(f"Testing input: {test.decode(errors='ignore')!r}")

        try:
            result = check_secret(test)
            print(f"  Result: {result}, Progress: {progress}/{len(SECRET)}")
        except SystemError as e:
            print(f"  💥 CRASH: {e}")

        print()

    print("=" * 60)
    print("To fuzz this vulnerability with FuzzForge:")
    print("  ff init")
    print("  ff workflow run atheris_fuzzing .")
    print("=" * 60)
