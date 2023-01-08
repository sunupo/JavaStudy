[How to iterate any Map in Java](https://www.geeksforgeeks.org/iterate-map-java/?ref=leftbar-rightbar)
1. Iterating over Map.entrySet() using For-Each loop :
```java
for (Map.Entry<String,String> entry : gfg.entrySet()) 
     System.out.println("Key = " + entry.getKey() +
                             ", Value = " + entry.getValue());
```

2. Iterating over keys or values using keySet() and values() methods
```java
 // using keySet() for iteration over keys
for (String name : gfg.keySet())
    System.out.println("key: " + name);

 // using values() for iteration over values
for (String url : gfg.values())
    System.out.println("value: " + url);
```

3. Iterating using iterators over Map.Entry<K, V>

```java
         
        // using iterators
        Iterator<Map.Entry<String, String>> itr = gfg.entrySet().iterator();
          
        while(itr.hasNext())
        {
             Map.Entry<String, String> entry = itr.next();
             System.out.println("Key = " + entry.getKey() + 
                                 ", Value = " + entry.getValue());
        }
```

4. Using forEach(action) method : 
```java
gfg.forEach((k,v) -> System.out.println("Key = "
                + k + ", Value = " + v));
```

5. Iterating over keys and searching for values (inefficient)

```java
// looping over keys
        for (String name : gfg.keySet()) 
        {
            // search  for value
            String url = gfg.get(name);
            System.out.println("Key = " + name + ", Value = " + url);
        }
```