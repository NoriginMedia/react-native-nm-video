# React Native custom Video module with DRM support

# Installation

## For usage in project

- Add the next dependency in `package.json` manually (since it's not NPM registered module)
```
"dependencies": {
    ...
    "react-native-nm-video": "git+https://github.com/NoriginMedia/react-native-nm-video.git"
    ...
}
```


- run `npm install`

- run `react-native link` so the lib will be automagically linked to your react-native iOS and Android projects

- in JS:
```javascript
import Video from "react-native-nm-video";

...

<Video {...props} />
```

## For development

- clone the repo `git@github.com:NoriginMedia/react-native-nm-video.git`

- run `npm install` in the root folder (installs ESLint etc.)

- `cd /examples`

- run `npm install` in `examples` folder (installs react-native for local testing in the empty project)

- run `react-native link` so the `react-native-nm-video` package get linked to your `example` subproject for iOS and Android automatically

### Android

- open Android Emulator

- in `example` folder run `npm run start:android`. Or `start:android` task from your IDE npm scripts window

- after the changes made to native code, run `npm run reinstall` to reinstall dependency with updated code.

- `start:android` again to rebuild with updated native code.

- Official guide on native UI modules: [here](https://facebook.github.io/react-native/docs/native-components-android.html)

- Standard `<Video />` component offered by the community that might be used as an example: [here](https://github.com/react-native-community/react-native-video/tree/master/android)

### iOS

- in `example` folder run `npm run start:ios`. Or `start:ios` task from your IDE npm scripts window

- after the changes made to native code, run `npm run reinstall` to reinstall dependency with updated code.

- `start:ios` again to rebuild with updated native code.

- Official guide on native UI modules: [here](https://facebook.github.io/react-native/docs/native-components-ios.html)

- Standard `<Video />` component offered by the community that might be used as an example: [here](https://github.com/react-native-community/react-native-video/tree/master/ios)

### Notes

- `examples` subproject has a dependency on the outer folder package (which is `react-native-nm-video itself) for testing

- `examples/index.<ios|android>.js` is an entry point to the app for respective platform.

- `Video.js` is a React component that requires native component named RCTNMVideo (React Norigin Media Video).

- Sources for native modules are in respective folders, `android` and `ios`.

- `example` folder is excluded when installing package in some external project (see `.npmignore`)
