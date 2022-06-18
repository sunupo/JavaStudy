/**
 * jjs 执行 JavaScript：
 * 1.JavaScript 中调用 Java
 * 2.控制台执行 jjs src/main/java/com/java/sjq/base/nashorn/sample.js
 */

var BigDecimal = Java.type('java.math.BigDecimal');

function calculate(amount, percentage) {

    var result = new BigDecimal(amount).multiply(
        new BigDecimal(percentage)).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_EVEN);

    return result.toPlainString();
}

var result = calculate(568000000000000000023,13.9);
print(result);