# To Do List

This todo list was build on the [reagent-template](https://github.com/reagent-project/reagent-template) but also includes [cljs-ajax](https://github.com/JulianBirch/cljs-ajax) and a couple of modifications to the middleware.

Styling is mostly handled with [Marx](https://mblode.github.io/marx/), with some modifications.

## Starting it up

To start the To Do List, you should have lein installed.

Navigate into the project folder in the terminal of your choice and then enter:

```
lein do clean, run
```

That gets the server running so we can use localhost.

Next we'll get the project running. Start another terminal and enter:

```
lein figwheel
```

Great! Now that those are up and running, we can go to the browser. The project should be at:

```
http://localhost:3000
```

Once that's loaded, you can start adding to do items, mark them completed, or delete them entirely.

## Future Improvements

- The ability to edit the text of each to do item
- Stats for number of items complete/incomplete
- A "mark all as completed" option
- A filter to show/hide the completed items
- Improve styling to accommodate long text
