<!-- Improved compatibility of back to top link: See: https://github.com/othneildrew/Best-README-Template/pull/73 -->

<a name="readme-top"></a>





<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    
  </ol>
</details>

<!-- ABOUT THE PROJECT -->

## About The Project

[[Trackcentric Dev Portal]](https://dev.trackcentric.ph)
## Trakcentric
* Smart tracking technology using GPS
  * More accurate tracking of your vehicle’s activity and deliveries inside the vehicle 


* Easy-to-Use Web Desktop and Driver Apps
  * Simple and efficient ways to track your fleet using Trackcentric mobile app for your drivers and Trackcentric portal for your transport planners

* Optimized Devices
  * We test and make sure that all the mobile devices you use are activated, tested, and ready for use

* Highly integratable
  * Trackcentric can be interfaced with your existing ERP system and other truck planning software

* Intelligent insights
  * Trackcentric provides operational data such as travel time, waiting time, truck bans and estimated delivery time




<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Built With

Trakcentric is composed of a Web Portal and a Mobile Application.

The Web portal is built with the following:
- [![Angular][angular.io]][angular-url]
- [![Laravel][laravel.com]][laravel-url]
- [![Bootstrap][bootstrap.com]][bootstrap-url]

The mobile application is built with
- [![Kotlin][kotlinlang.org]][kotlin-url]

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- GETTING STARTED -->



### Installation
#### SETTING UP TRACKCENTRIC PRODUCTION SERVER
---

##### LOGIN TO SERVER

```
ssh <user>@<dev-ip>
```

##### UPDATE THE SYSTEM

```
sudo apt-get update
sudo apt-get upgrade
```

---

##### SETUP DATABASE

###### **Install MySQL**

```
sudo apt-get install mysql-server
```

*set root user password: `<Root Password>`*

**Setup MYSQL Timezone support**

Read more here:

https://dev.mysql.com/doc/refman/5.5/en/time-zone-support.html

**Create the database**

to enter the mysql command line

```
mysql -uroot -p@<root password>
```

in the command line create the database

```
CREATE DABATASE pod;
```

then type `exit` to quit mysql command line

**Run the pre-run.sql file to setup the database**

```
mysql -uroot -p@<root password> pod < pre-run.sql
```

---

### SETUP NODE

**Install NVM [Node Version Manager]**

```
sudo apt-get install build-essential libssl-dev
curl https://raw.githubusercontent.com/creationix/nvm/v0.33.1/install.sh | bash
source ~/.profile
```

**Install Node 6**

```
nvm install 6
```

---

### SETUP NODE-GYP

node-gyp is a cross-platform command-line tool written in Node.js for compiling native addon modules for Node.js.

**Install checkinstall**

checkinstall is a simple program which monitors the installation of files, and creates a Debian package from them. (We will use this to install python)

```
sudo apt-get install checkinstall
```

**Install Python 2.7**

Python 2.7 is a dependency of node-gyp so we need to install it.

* Download python

```
    mkdir Downloads
    cd Downloads
    wget https://www.python.org/ftp/python/2.7.13/Python-2.7.13.tgz
```

* Extract

```
    tar -xvf Python-2.7.13.tgz
```

* Build the package using checkinstall

```
    cd Python-2.7.13
    ./configure
    make
    sudo checkinstall
```

* Just follow the installation process (It may take few minutes to build and install the package, so be patient).

* Delete the Downloads folder since we don't need it anymore

```
    sudo rm -rf Downloads
```

**Install node-gyp**

```
npm install -g node-gyp
```

---

### INSTALL GLOBAL NPM DEPENDENCIES

```
npm install -g loopback-cli
npm install -g loopback-sdk-angular-cli
npm install -g gulp
npm install -g bower
npm install -g forever
```

---

### SETUP GIT HOOK

We will use git hook so that whenever developers pushes commits to a remote source the server will be updated

**Create needed directories**

```
cd ~
mkdir trackcentric
mkdir trackcentric.git
```

**Create a post-receive git hook**

```
cd trackcentric.git
git init --bare
vi hooks/post-receive
```

*add the following to the post-receive file **(hit ins/insert to edit)***

```
HOME=/home/marks/
. $HOME/.profile
GIT_WORK_TREE=$HOME/trackcentric git checkout -f
cd $HOME/trackcentric
sh ./bootServer.sh
```

*save and quit **(hit esc, type `:wq`, hit enter)***

**Make post-receive file executable**

```
chmod +x hooks/post-receive
```

**Update .bashrc**

If you do `vi ~/.bashrc` in your terminal you will see these lines at the bottom of the file

```
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"  # This loads nvm
```

These lines will not be executed because of the conditions before it

But we need those lines so node should work with the post-receive script

For it to work, we need to move these lines on top of the .bashrc

Now hit `ins/insert` to edit the file

Move those lines

And hit `esc`, type `:wq`, and hit `enter` to save and quit

---

#### ENABLE NODE TO USE PORT 80

so that trackcentric can be accessed directly without using port

```
sudo apt-get install libcap2-bin
sudo setcap 'cap_net_bind_service=+ep' $(which node)
```

---

#### CREATE THE LOGS DIRECTORY

deploy logs will be written here

```
mkdir logs
```

---

#### SETUP YOUR MACHINE TO PUSH UPDATES TO PRODUCTION

#### CREATE REMOTE SOURCE

```
git remote add production ssh://<user>@<dev-ip>/home/<user>/trackcentric.git
```

#### DO INITIAL PUSH TO REMOTE SERVER

```
git push production +master:refs/heads/master
```

now use this command whenever you want to update the server

```
git push production master
```

#### ALTER DATABASE TO UTF8MB4_UNICODE
```
ALTER DATABASE pod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE TABLENAME convert to character set utf8mb4 collate utf8mb4_unicode_ci;

ALTER TABLE TABLENAME MODIFY COLUMNNAME VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
#### Build Mobile App using Gradle
    android {
    compileSdkVersion 28
    defaultConfig {
        applicationId "com.appcentric.trackcentricmobile"
        minSdkVersion 23
        targetSdkVersion 28
        versionCode 6
        versionName "Internal 1.1"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    lintOptions {
        checkReleaseBuilds false
        // Or, if you prefer, you can continue to check for errors in release builds,
        // but continue the build even when errors are found:
        abortOnError false
        }
    }


<!-- USAGE EXAMPLES -->


<!-- ROADMAP -->

## Roadmap

- [x] Launch Trackcentric Mobile to specific customers
- [x] Launch Trackcentric Mobile with ERP Integration (SAP and Netsuite)
- [ ] Build the Trackcentric Marketplace
- [ ] Launch nationwide

    

<p align="right">(<a href="#readme-top">back to top</a>)</p>

[contributors-shield]: https://img.shields.io/github/contributors/othneildrew/Best-README-Template.svg?style=for-the-badge
[contributors-url]: https://github.com/othneildrew/Best-README-Template/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/othneildrew/Best-README-Template.svg?style=for-the-badge
[forks-url]: https://github.com/othneildrew/Best-README-Template/network/members
[stars-shield]: https://img.shields.io/github/stars/othneildrew/Best-README-Template.svg?style=for-the-badge
[stars-url]: https://github.com/othneildrew/Best-README-Template/stargazers
[issues-shield]: https://img.shields.io/github/issues/othneildrew/Best-README-Template.svg?style=for-the-badge
[issues-url]: https://github.com/othneildrew/Best-README-Template/issues
[license-shield]: https://img.shields.io/github/license/othneildrew/Best-README-Template.svg?style=for-the-badge
[license-url]: https://github.com/othneildrew/Best-README-Template/blob/master/LICENSE.txt
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://linkedin.com/in/othneildrew
[product-screenshot]: images/screenshot.png
[next.js]: https://img.shields.io/badge/next.js-000000?style=for-the-badge&logo=nextdotjs&logoColor=white
[next-url]: https://nextjs.org/
[react.js]: https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB
[react-url]: https://reactjs.org/
[vue.js]: https://img.shields.io/badge/Vue.js-35495E?style=for-the-badge&logo=vuedotjs&logoColor=4FC08D
[vue-url]: https://vuejs.org/
[angular.io]: https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white
[angular-url]: https://angular.io/
[svelte.dev]: https://img.shields.io/badge/Svelte-4A4A55?style=for-the-badge&logo=svelte&logoColor=FF3E00
[svelte-url]: https://svelte.dev/
[laravel.com]: https://img.shields.io/badge/Laravel-FF2D20?style=for-the-badge&logo=laravel&logoColor=white
[laravel-url]: https://laravel.com
[bootstrap.com]: https://img.shields.io/badge/Bootstrap-563D7C?style=for-the-badge&logo=bootstrap&logoColor=white
[bootstrap-url]: https://getbootstrap.com
[jquery.com]: https://img.shields.io/badge/jQuery-0769AD?style=for-the-badge&logo=jquery&logoColor=white
[jquery-url]: https://jquery.com
[kotlin-url]: https://kotlinlang.org
[kotlinlang.org]: https://img.shields.io/badge/kotlin-purple?style=for-the-badge&logo=kotlin&logoColor=white
