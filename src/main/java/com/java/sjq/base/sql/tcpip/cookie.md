**Cookie相关的Http头**

  有 两个Http头部和Cookie有关：Set-Cookie和Cookie。

  Set-Cookie由服务器发送，它包含在响应请求的头部中。它用于在客户端创建一个Cookie

  Cookie头由客户端发送，包含在HTTP请求的头部中。注意，只有cookie的domain和path与请求的URL匹配才会发送这个cookie。



挡在服务器通过response 设置cookie之后，浏览器下次访问会自动带上cookie
