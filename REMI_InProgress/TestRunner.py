import os
from urllib import request
from bs4 import BeautifulSoup
from torrequest import TorRequest
from hashlib import sha256
import queue

grailText = "Follow this link to get a complete"

def main():
    url = "https://foodwishes.blogspot.com/"
    site = request.urlopen(url="https://foodwishes.blogspot.com/").read().decode('utf8')
    #print(site)
    soup = BeautifulSoup(site, features="lxml")
    for link in soup.find_all('a')[:8]:
        #print(link.get('a'), link.get('href'), link.get('id'), link.get('a class'))
        href = link.get('href')

        foodWishScrape(href) if(str(link).find(grailText) == -1) else allRecipeScrape(href)
        
        print(link)

def foodWishScrape(link):

    return link

def allRecipeScrape(link):
    print("allscrape: ", link)
    arSite = request.urlopen(url= link).read().decode()
    arSoup = BeautifulSoup(arSite, features= 'lxml')
    with open("alltestoutSite.txt", "w", encoding= "utf8") as f:
        f.write(arSite)
    with open("alltestoutSoup.txt", "w", encoding= "utf8") as f2:
        f2.write(str(arSoup))

    return link

#def arsTinker(page):
    



main()