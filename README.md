DeskChan Launcher
=================

An application that downloads a DeskChan instance and keeps it up to date.

_Для русскоговорящей аудитории в [Wiki](https://github.com/kozalosev/DeskChan-Launcher/wiki) подготовлен перевод
данного документа на русский язык._


Overview
--------

### Problem

This application respects your laziness and understands that it's hard to upgrade DeskChan every time when a new build
is released. In fact, at this moment we have two ways to do it manually:

#### First option:

1. Quit the application.
2. Download a distributive archive.
3. Delete all files and directories in your DeskChan directory, excluding:
   - `data` directory;
   - `plugins` directory **unless it contains third-party plugins**. In this case, preserve all third-party plugins subdirectories and delete everything else.
4. Extract the new archive.
5. Launch a new version of the application.

#### Second option:

1. _Ditto._
2. _Ditto._
3. Extract the archive and overwrite all files.
4. Gets rid of all deprecated files manually.
5. _Ditto._

And you have to repeat all these steps over and over, again and again for every new build of DC. Tediously, isn't it?


### Solution

Probably, you're already curious what I suggest to do with it and how it **should** work. OK, let's see:

1. You launch the application via the launcher or it's started automatically during OS load.
2. Amazing! DeskChan is being updated automatically! What a crazy thing!
3. Profit! It just launches! That's it.


### Reality

Unfortunately, the launcher is not working in this way for now. Currently, it's only able to:

- check if you're using the latest version of it or not;
  - if you don't:
    - download a distribution,
    - extract files and delete it;
- launch the DeskChan application;
- terminate itself.

It's sort of a rough overview but illustrates a general idea. For example, the launcher also can make itself a startup
application. We'll discuss it in detail later.

More functionality will be available in future releases.


Architecture
------------

As of **v0.1.1-dev**, the launcher consists of 3 parts:

| Module              | Files                                           | Description                                                                     |
| ------------------- | ----------------------------------------------- | ------------------------------------------------------------------------------- |
| Core library        | _DCL-CORE.jar_                                  | Contains all logic that is responsible for the version resolving and installation. |
| Command line module | _dcl.exe_, _dcl_                                | Is responsible for interacting with the user via terminal in a text mode.       |
| Graphical module    | _DeskChan-Launcher.exe_,<br>_DeskChan-Launcher_ | Provides a graphical user interface using JavaFX library.                       |

<div align="center">
    <img src="https://i.imgur.com/Tg6U5Hu.png">
</div>

As you can see from the diagram above, both command line and graphical modules depend on the core library (obviously,
isn't it?), but they are completely independent of each other.


### Why do we need 2 modules?

I am intentionally maintaining both UI modules as they both have unique features that are based on CLI or GUI workflow. Such features are briefly described in the next sections.


#### Command line interface module

<div align="center">
    <img src="https://i.imgur.com/ViQzRQv.png">
</div>

The command line module can be considered as more stable and has more features compared to the GUI one.

By default, it:

- uses the system language;
- installs DeskChan into the directory of the same name if cannot find it already installed and up to date;
- launches the application.

As usual, it supports command line arguments, that allow users to configure various aspects of the launcher's work, listed below:

| Argument                     | Description                                                                                                                                                                                                                                                                         |
| ---------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `--deskchan-update-required` | If you pass this argument, the launcher just prints "true" or "false", depending on the availability of a newer release of DeskChan on GitHub or not, and terminates.                                                                                                               |
| `--launcher-update-required` | Same as the previous one, but for the launcher itself.                                                                                                                                                                                                                              |
| `--deskchan-version`         | Prints the current version of DeskChan that is installed under the control of this launcher and terminates.                                                                                                                                                                         |
| `--launcher-version`         | Just prints the current version of itself.                                                                                                                                                                                                                                          |
| `--path`                     | Using this argument, you may define another location for installation. However, the launcher will copy itself there to be along with DeskChan by all means.                                                                                                                         |
| `--locale`                   | If you don't want to use a default locale, you may set another here. It takes strings like `ru` or `ru_RU`. Currently, only two languages are supported: **English** and **Russian**.                                                                                               |
| `--delay`                    | Determines the time in seconds that must pass between the intent of application to quit and the moment when the window will be actually closed. It's quite useless when the launcher has been launched via terminal but very useful when it was launched via a desktop environment. |
| `--autorun`                  | By default, the launcher asks you should it be a startup application or not during the installation process. But you may manually set this flag in advance. Also, it can be used to change the setting during usual launch, not the installation.                                   |
| `--no-autorun`               | See the description of the previous argument. This one is the opposite to it.                                                                                                                                                                                                       |
| `--preserve-distribution`    | By default, the launcher deletes a distributive archive after the installation. You can disable this behavior using this flag.                                                                                                                                                      |


#### Graphical user interface module

<div align="center">
    <img src="https://i.imgur.com/SGREDOl.png">
</div>

As of **v0.1.1-dev** the launcher can be launched in the GUI mode to present a pretty window to the user.
This mode lacks a way to change program's behavior using command line arguments, but the most useful options were
implemented as controls. However, they are only equivalents for `--path` and `--autorun`/`--no-autorun`.
Moreover, you may set them only during the initial installation process. Thus, you cannot disable
autorun on startup at any time via graphical interface today. I'm going to solve this problem in the future, though.


FUTURE
------

_One of [CORRUPTOR2037](https://github.com/CORRUPTOR2037)'s favorite buzzwords._


### What will be in v0.1.2-dev:

- Support for files-based distributions against the archive-based ones that are used now. What does it mean? Currently,
  the launcher always has to download a whole 20+ Mb distributive archive to extract it and install the application.
  Even if the update affects only several small-sized files (frankly speaking, it's a hardly possible case nowadays:
  all the code of DeskChan with all dependencies are packed into one single file (`bin/DeskChan.exe`); however, it's
  still just a half of the whole archive). A solution requires a lot of work on the server side too (probably,
  for the time being, I'll even have to keep copies of distributions in a proper format on my own server) and will take
  most of the time that I'll spend working on the next release (along with the following feature).
- Support for consistency-correct updates. This means that the launcher will be able to not only _add_ and _replace_
  files but **delete** the obsolete ones.

#### What is likely to be in v0.1.2-dev (or will be in v0.1.3-dev):

- A plugin for DeskChan itself that will be able to take responsibility for some features and options.
  For example, it will manage the autorun option (especially useful for graphical interface module) and be used as
  a fallback to notify the user about the availability of newer versions.


### What will be in v0.1.3-dev

- A way to install a specific version of DeskChan.
- Support for other repositories except the default one. This feature makes it possible to split releases into different
branches (e.g. _stable_, _beta_, _dev_ or something like them).

#### What will possibly be in v0.1.3-dev

- Plugin management via the plugin. I think the plugin should not be just a middleware between two applications. It can
be a more complex, separate artifact with its own specific functionality based on the already written infrastructure.
A plugin management system, for instance, must be pretty similar to the launcher: version resolving, files-based
downloading, consistency-correct updates, and so on. However, it must have additional features: dependency resolving,
searching over a catalog of plugins, etc.  
_Maybe I change my mind later, and it will be an independent plugin whose history will be written in another repository.
But for now, I think so._


Building
--------

The project has a Gradle wrapper configured, so you can use the following command template:

- on **Windows**: `gradlew <task>`
- on **Linux**: `./gradlew <task>`

Here is a list of some main tasks in which you might be interested:

| Task               | Description                                                                                                                                  |
| ------------------ | -------------------------------------------------------------------------------------------------------------------------------------------- |
| `buildDistZip`     | Builds the project and creates a distribution as a zip archive.                                                                              |
| `wrapFilesWithDir` | Does the same as the previous task, but not generate the archive. You can find the output in the `build/DeskChan-Launcher <version>` folder. |
| `run`              | Just compiles all classes, resources and runs the application. The working directory will be set to the `build/classes` folder.              |


Afterword
---------

Note that this application is under a **very slow** development, and it's normal for this repository to not have new
commits for months. Just keep this in mind.

If you found a mistake in this text (or in other English texts as well), let me know! I will appreciate your help to
make my English better :)

Besides usual GitHub means and emails, you may contact me (or someone else from
[DeskChan Project](https://github.com/DeskChan)) via [Telegram](https://t.me/dc_flood). It's even a preferable variant,
I suppose. And don't let Russian letters scare you!
