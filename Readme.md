#Loop counter

This small project count the number of loops in a given project that can be transformed using our technique.

Some results so far:

| Project    | Total Loops | Transformable | Percent |
|---         |---          | ---          | ---     |
|Common Math | 2851        | 375 | 13.15 |
|OpJ Machine Learning | 536 | 57 | 10.63 |
|OpJ Image Processing | 595 | 71 | 11.93 |
|OpJ Sound Processing | 80  | 14 | 17.5  |

The first column shows the project, the second, the total number of loops within that project. The *Transformable* column shows how many loops conform to the pattern over which the optimization can work. Finally, the percent column gives the percentage these loops represent of the total.