Integer value = 2
Integer result = 0
switch value {
    case 1:
        result = 10
    case 2, 3:
        result = 20
    case 4:
        result = 30
    default:
        result = 0
}
export result

