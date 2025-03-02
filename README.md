# Peters solution to SP-1: Building a Movie Repository

BuildMain.java has main method to build the database (one time operation)

FuncMain.java has main method which shows of the functionality

Database uses four entities:
- Genre
- Movie
- Person (contains actors, directors, and all other crew)
- Credit (is a join table between Movie and Person, that have extra fields job and character)