try:
    a = float(input("Enter number a: "))
    b = float(input("Enter number b: "))
    print(a + b)
except ValueError:
    print("Incorrect input")