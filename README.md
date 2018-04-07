# chili-game-ext

Split the 2D isometric mmo in development inside the chili-core repository into this new repo. This allows us to make faster changes in core as new projects require them. Because then we do not need to rebuild the game on each change. Also, it reduces build times for chili-core, and the game! It makes sense since there are now multiple projects depending on chili-core. 
