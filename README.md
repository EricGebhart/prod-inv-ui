# prod-inv-ui

A [re-frame](https://github.com/Day8/re-frame) / clojurescript / react application.
This is a ui for a backend server which contains product inventory levels and history.

But it also runs standalone as an SPA with generated data.

I obviously went a little overboard. You can choose to use the server or not.

Both Bonus features listed in the goals below are present.

* The ability to add a new inventory record for the currently selected product.
* The ability to choose any number of product for the chart.

The server side project written in python/flask/sqlalchemy is 
[here](http://github.com/ericgebhart/product-inventory.git).

# improvments left.

I stored the timestaps as epoch numbers. This makes it easy to do a
lot of different things easily with the dates. But I never got around
to adding a render function to the timestamp column in the history
table.  They show nicely on the graphs though.

There are a few things which I would extract out of this to make more
useful react components.  The charts could be made much more generic
and reusable.  The component is reusable, but creating the data for
the chart is a matter of creating a datastructure with all the right
settings.  Still it works fine. And my goal here was to just create 
a UI not a set of reusable libraries.

I could also do more with co-effects and interceptors but thats not
a path I want to take at the moment.

Testing is reasonable.  I could go further, but there really isn't much
to test. Some scenarios with sequences of event dispatches could be
interesting.


# these are the goals.  On verra.

**Front (client side)** 


*It should include three main elements:* 

1- A graph of inventory level(y-axis) vs. date(x-axis) of the selected product 

2- Table of data filtered on the selected product: product_id; product_name; date; inventory_level

3- Dropdown/Select option to choose the product ID or product name to visualize

4- **(Bonus)** Add a button that allows data table editing. Add the possibility to change the "inventory level"  and send request to change it in the back-end 

5- (**Bonus)** Add the possibility to choose multiple products and visualize them in the same graph

**Back (server side)**

Storing the data in a simple flat file. 
Implement a simple API (post, get,...). 

**Unit test**

Write some basic unit test (client and server side)

## Annexe

**Constraints**

1) front-end code and back-end code are separated in two different projects

2) Communication between back and front is done by a rest API or GaphQL

**Type of data**

You can create the sample of data you want with at least those elements: 

```
product_id(int); product_name(String); date(String: "dd-mm-yyyy"); inventory_level(int)
```


## To start.

Install [leiningen](http://leinengin.org)


## get the dependencies.

```lein deps
   yarn install
   ## or npm install
   npm install highcharts
```

## The project template

This command was used to create this initial project.

```
lein new re-frame prod-inv-ui +re-com +cider +garden +10x +test
```

You can read about the [re-frame template here.](https://github.com/Day8/re-frame-template/) 

* [re-com](https://github.com/Day8/re-com) are components, I may not have used them. 
* [cider](https://github.com/clojure-emacs/cider) is the clojure repl and debugging interface in emacs.
* [garden](https://github.com/noprompt/garden) is a way to create CSS with clojure data. 
It is to css as [hiccup](https://github.com/weavejester/hiccup) is to html.
* re-frame-10x is a way to see inside react, read about it [here](https://github.com/Day8/re-frame-10x). 
* test integrates basic testing as in [cljs.test.](https://github.com/clojure/clojurescript/blob/master/src/main/cljs/cljs/test.cljs).


## Development

I developed this in isolation of the backend in the product_inventory server project. 
Then switched the data events to send requests to the server once everything was working 
with mocked data. So ideally, you'll want to be running that server too.

## Development Mode

### Start Cider from Emacs:

Refer to the [shadow-cljs Emacs / CIDER documentation](https://shadow-cljs.github.io/docs/UsersGuide.html#cider).

The mentioned `dir-local.el` file has been created.

### Compile css:

Compile css file once.

```
lein garden once
```

Automatically recompile css file on change.

```
lein garden auto
```

### Run application:

```
lein clean
lein dev
```

shadow-cljs will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:8280](http://localhost:8280).

## Open 10x Panel :

To use `re-frame-10x` for debugging your app: 
  1. click on the application, minimal through it is, to give it "input<project-name> focus" (you want to be sure that any key presses are going to your new app)
  2. press `Ctrl-H` and you should see the `re-frame-10x` panel appear on the right side of the window

Sometimes achieving Step 1 on a really simple app - one without widgets - can be fiddly, 
because the browser itself hogs "input focus" and grabs all the keystrokes (like `Ctrl-H`) which don't
then make it through to your app. You may need to be determined and creative with Step 1. 

## Hot Reloading Is Now Go

If you now edit files, like perhaps `/src/cljs/<project-name>/views.cljs`, 
[Figwheel](https://figwheel.org) will automatically 
recompile your changes and "hot load" them into your running app, without your app needing 
to be re-started. The resulting fast, iterative workflow tends to make you very productive, and 
is cherished by those lucky enough to experience it.

### debug?:

In _project-name.config_, there is a variable called `debug?`, which
defaults to _true_. However, for the `min` build (look inside of `project.clj`), this variable is
re-defined to _false_.

When `debug?` is true, we include `(enable-console-print!)`. If you, for example, 
you wrap your `println`s with a `when` block as show below, then you will get logs 
printed to the browser's console for the `dev` build and not the `min` build.

```clojure
(when config/debug?
  (println "dev mode"))
```

### Run tests:

Install karma and headless chrome

```
npm install -g karma-cli
```

And then run your tests

```
lein clean
lein run -m shadow.cljs.devtools.cli compile karma-test
karma start --single-run --reporters junit,dots
```

## Production Build

This will compile the project into javascript for production use.

```lein prod```
