Integer a = 10
if a > 5 {
    for i in 1..3 {
        a = a + i
    }
} else {
    a = 0
}
switch a {
    case 16
        a = 100
    default
        a = 200
}
export a

