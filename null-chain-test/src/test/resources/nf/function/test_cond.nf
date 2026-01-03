fun testCond(int n)Integer {
    if n <= 1 {
        return 100
    }
    return 200
}
Integer result = testCond(1)
echo "result = {result}"
export result
