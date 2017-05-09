max = 20

base = 32

f = File.open("rbdataoutrsam4.txt", "w")

for i in 0..max
    size = base * 2**i
    puts "Generating #{size} byte file. (#{size/(2**20)}MB)"
    `dd if=/dev/urandom of=test_files/randruby.dat  bs=#{size}  count=1 2> /dev/null`
    `java -jar filepay.jar -r test_files/randruby.dat`
    puts "Starting timed run."
    start = Time.now()
    `java -jar filepay.jar -p 0xfe88c94d860f01a17f961bf4bdfb6e0c6cd10d3fda5cc861e805ca1240c58553 test_files/randruby.dat -c -m 4 -r`
    endt = Time.now()
    puts "Time was #{endt-start}s."
    f.write("#{size} #{endt-start}\n")
end