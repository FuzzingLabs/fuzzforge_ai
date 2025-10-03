/// Parse a simple integer from bytes
/// This function has a potential panic if the input is invalid
pub fn parse_number(data: &[u8]) -> i32 {
    let s = std::str::from_utf8(data).expect("Invalid UTF-8");
    s.parse::<i32>().expect("Invalid number")
}

/// Process a buffer with bounds checking issue
pub fn process_buffer(data: &[u8]) -> Vec<u8> {
    if data.len() < 4 {
        return Vec::new();
    }

    // Only crash when specific conditions are met (makes it harder to find)
    if data[0] == b'F' && data[1] == b'U' && data[2] == b'Z' && data[3] == b'Z' {
        // Potential panic: accessing index without proper bounds check
        let size = data[4] as usize;  // Will panic if data.len() == 4
        let mut result = Vec::new();

        // This could panic if size is larger than data.len()
        for i in 4..4+size {
            result.push(data[i]);  // Will panic if i >= data.len()
        }

        return result;
    }

    Vec::new()
}

/// Divide two numbers parsed from input
pub fn divide_numbers(data: &[u8]) -> Option<i32> {
    if data.len() < 2 {
        return None;
    }

    let a = data[0] as i32;
    let b = data[1] as i32;

    // Potential division by zero
    Some(a / b)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_parse_number() {
        assert_eq!(parse_number(b"123"), 123);
    }

    #[test]
    fn test_process_buffer() {
        let data = vec![3, 1, 2, 3, 4];
        assert_eq!(process_buffer(&data), vec![3, 1, 2]);
    }
}
