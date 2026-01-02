Integer sum = 0
for i in 1..10 {
    if i % 2 == 0 {
        continue
    }
    sum = sum + i
}
export sum

