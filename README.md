# WebView State save demo

### What restoreState() Actually Restores

The Bundle created by saveState() is primarily concerned with the state of the browser view, not the state of the web page's content. 

It restores things like:
- Navigation History: The back/forward list (WebBackForwardList) is restored. This is why canGoBack() still works and goBack() takes you to the previously visited page.
- Scroll Position: The x and y scroll offsets of the page are restored, so you appear in the same place.
- Zoom Level: The page's zoom factor is restored.
- Some Form Data: It can sometimes restore data in simple HTML form fields (<input>, <textarea>), but this is not always reliable, especially with complex forms manipulated by JavaScript.

### What is Lost (The JavaScript State)

The entire JavaScript runtime environment is discarded and rebuilt from scratch. 

This means:
- Global Variables: Any variables declared on the window object or in the global scope are reset to their initial values.
- DOM Manipulations: If your JavaScript added or removed elements, changed styles, or updated text content on the page
