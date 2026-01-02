Integer sum = 0
for i in 1..5 {
    for j in 1..5 {
        if j > 2 {
            breakall
        }
        sum = sum + j
    }
}
export sum

