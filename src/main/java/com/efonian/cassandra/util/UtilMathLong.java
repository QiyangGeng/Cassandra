package com.efonian.cassandra.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class for some math stuff with the long class
 */
public class UtilMathLong {
    public final static List<Long> SMALL_PRIMES = List.of(2L, 3L, 5L, 7L, 11L, 13L, 17L, 19L, 23L, 29L);
    
    private static final CacheLoader<Long, List<Long>> pFPRLoader = new CacheLoader<>() {
        @Override
        public List<Long> load(@NotNull Long aLong){
            return primeFactorizationPollardRhoPrivate(aLong);
        }
    };
    
    private static final LoadingCache<Long, List<Long>> pFRPCache = CacheBuilder.newBuilder()
            .maximumSize(1000).build(pFPRLoader);
    
    public static List<Long> primeFactorizationPollardRho(long num) {
        return pFRPCache.getUnchecked(num);
    }
    
    /**
     * @return a list of the prime factors of num
     */
    private static List<Long> primeFactorizationPollardRhoPrivate(long num) {
        if(num <= 1)
            return List.of();
        if(isPrime(num))
            return List.of(num);
        
        List<Long> primeFactors = new ArrayList<>();
        
        while(num % 2 == 0) {
            primeFactors.add(2L);
            num /= 2;
        }
        
        if(num <= 1)
            return primeFactors;
        if(isPrime(num)) {
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
                long z = 0;
            }
        } while(d == num);
        
        primeFactors.addAll(primeFactorizationPollardRho(d));
        primeFactors.addAll(primeFactorizationPollardRho(num / d));
        
        return primeFactors.stream().sorted().collect(Collectors.toList());
    }
    
    public static long abs(long num) {
        return num < 0 ? -num : num;
    }
    
    public static boolean isPrime(long num) {
        if(num == 1)
            return false;
        if(SMALL_PRIMES.contains(num))
            return true;
        
        if(num % 2 == 0 || num % 3 == 0)
            return false;
        
        long lim = sqrtLong(num);
        for(long i = 5; i <= lim; i += 6) {
            if(num % i == 0 || num % (i + 2) == 0)
                return false;
        }
        return true;
    }
    
    public static long sqrtLong(long num) {
        return exp(ln(num)/2.0);
    }
    
    private static long exp(double num) {
        return Math.round(Math.exp(num));
    }
    
    private static double ln(long num) {
        return Math.log(num);
    }
    
    public static boolean isProbablePrimeFermat(long num, long k) {
        if(SMALL_PRIMES.contains(num))
            return true;
    
        if(num % 2 == 0 || num % 3 == 0)
            return false;
        
        for(long i = 0; i < k; i++) {
            long a = randomLongBoundedInc(2, num - 2);
            if(modExp(a, num - 1, num) != 1)
                return false;
        }
        return true;
    }
    
    /**
     * Miller-Rabin test, O(k*log^3(num))
     */
    public static boolean isProbablePrimeMillerRabin(long num, long k) {
        if(SMALL_PRIMES.contains(num))
            return true;
    
        if(num % 2 == 0 || num % 3 == 0)
            return false;
        
        long s = 0;
        long d = num - 1;
        while(d % 2 == 0) {
            s += 1;
            d /= 2;
        }
        
        for(long i = 0; i < k; i++) {
            long a = randomLongBoundedInc(2, num - 2);
            long x = modExp(a, d, num);
            
            long y = 1;
            for(long j = 0; j < s; j++) {
                y = modExp(x, 2, num);
                if(y == 1 && x != 1 && x != num - 1)
                    return false;
                x = y;
            }
            if(y != 1)
                return false;
        }
        return true;
    }
    
    public static long modExp(long a, long b, long c) {
        return a ^ b % c;
    }
    
    /**
     * Returns the greatest common divisor of two longs.
     */
    public static long gcd(long a, long b) {
        if(a == 0 && b == 0)
            return 0;
        
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
    
    /**
     * @return true if n divides k, false otherwise
     */
    public static boolean divides(long n, long k) {
        if(n == 0)
            return false;
        
        return k % n == 0;
    }
    
    /**
     * Generate bounded random numbers inclusive of limits
     */
    public static long randomLongBoundedInc(long lowerLimit, long upperLimit) {
        return lowerLimit + (long) (Math.random() * (upperLimit - lowerLimit + 1));
    }
}
