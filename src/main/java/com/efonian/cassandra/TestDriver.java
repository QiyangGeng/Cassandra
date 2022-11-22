package com.efonian.cassandra;

import com.efonian.cassandra.util.UtilMathLong;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestDriver {
    public static void main(String[] args) {
        try {
            isPrimeTest();
            primeFactorizationPollardRhoTest();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void isPrimeTest() throws IOException {
        List<Long> primes = Files.lines(Path.of("./src/main/resources/lookup/prime10000.txt"))
                .map(l -> l.trim().split("\\s+"))
                .flatMap(Arrays::stream)
                .map(Long::parseLong)
                .collect(Collectors.toList());
        
        for(long i = 0; i < 100000; i++) {
            boolean result = UtilMathLong.isPrime(i);
            if(result != primes.contains(i))
                System.out.println("Failed: i = " + i);
        }
    }
    
    public static void primeFactorizationPollardRhoTest() {
        long min = 90000000;
        long max = 100000000;
        
        long sT = System.nanoTime();
        for(long i = min; i < max; i++) {
            UtilMathLong.primeFactorizationPollardRho(i);
//            List<Long> factors = UtilMathLong.primeFactorizationPollardRho(i);
//            System.out.println("i = " + i + ": " + factors);
        }
        long eT = System.nanoTime();
        long dT = (eT - sT) / 1000000;
        System.out.println("Time for custom modPow: " + dT + " ms");
    
        sT = System.nanoTime();
        for(long i = min; i < max; i++) {
            primeFactorizationPollardRho(i);
//            List<Long> factors = primeFactorizationPollardRho(i);
//            System.out.println("i = " + i + ": " + factors);
        }
        eT = System.nanoTime();
        dT = (eT - sT) / 1000000;
        System.out.println("Time for plain modPow: " + dT + " ms");
    }
    
    private static List<Long> primeFactorizationPollardRho(long num) {
        List<Long> primeFactors = new ArrayList<>();
        
        if(num <= 1)
            return primeFactors;
        
        while(num % 2 == 0) {
            primeFactors.add(2L);
            num /= 2;
        }
        
        if(num <= 1)
            return primeFactors;
        if(UtilMathLong.isPrime(num)) {
            primeFactors.add(num);
            return primeFactors;
        }
        
        long x = 2, y = 2;
        long c = 0;
        long d;
        
        do {
            c++;
            d = 1;
            while(d == 1) {
                x = (x * x + c) % num;
                y = (y * y + c) % num;
                y = (y * y + c) % num;
                d = gcd(abs(x - y), num);
            }
        } while(d == num);
        
        primeFactors.addAll(primeFactorizationPollardRho(d));
        primeFactors.addAll(primeFactorizationPollardRho(num / d));
        
        return primeFactors.stream().sorted().collect(Collectors.toList());
    }
    
    public static long abs(long num) {
        return num < 0 ? -num : num;
    }
    
    public static long gcd(long a, long b) {
        if(a == 0 && b == 0)
            return 0;
        
        a = abs(a);
        b = abs(b);
        
        if(a == 0)
            return b;
        else if(b == 0)
            return a;
        
        if(a > b)
            a %= b;
        else
            b %= a;
        
        if(a == 0)
            return b;
        else if(b == 0)
            return a;
        
        while(a != b) {
            if(a > b)
                a = a - b;
            else
                b = b - a;
        }
        return a;
    }
}
