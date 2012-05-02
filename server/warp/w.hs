{-# LANGUAGE TypeFamilies, QuasiQuotes #-}
{-# LANGUAGE MultiParamTypeClasses #-}
{-# LANGUAGE TemplateHaskell, OverloadedStrings #-}

module Main where

import Yesod

data HelloWorld = HelloWorld
instance Yesod HelloWorld

mkYesod "HelloWorld" [parseRoutes|
/		HomeR		 GET
/hello	HelloR		 GET
/index  IndexR		 GET
|]


getHomeR :: Handler RepHtml
getHomeR = defaultLayout [whamlet|
<a href=@{HelloR}>Go to hello page
|]

getHelloR :: Handler RepHtml
getHelloR = defaultLayout [whamlet|
<h1>Hello world!!




|]

getIndexR :: Handler RepHtml
getIndexR = defaultLayout $ do
--		  		$(widgetFile "sample")
--		  		content <- liftIO $ readFile "index.html"
				[whamlet|<h1>httpd               |]

main :: IO()
main = warp 8123 HelloWorld
