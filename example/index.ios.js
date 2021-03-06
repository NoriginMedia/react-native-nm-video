import React, {Component} from "react";
import {
  AppRegistry,
  StyleSheet,
  Text,
  View
} from "react-native";
import Video from "react-native-nm-video";

const styles = StyleSheet.create({
	container: {
		flex: 1,
		justifyContent: "center",
		alignItems: "center",
		backgroundColor: "#F5FCFF"
	},
	welcome: {
		fontSize: 20,
		textAlign: "center",
		margin: 10
	},
	instructions: {
		textAlign: "center",
		color: "#333333",
		marginBottom: 5
	}
});

export default class NMVideoExample extends Component {
	render() {
		return (
			<View style={styles.container}>
				<Text style={styles.welcome}>
					Welcome to React Native!
				</Text>
				<Text style={styles.instructions}>
					To get started, edit index.ios.js
				</Text>
				<Text style={styles.instructions}>
					Press Cmd+R to reload,{"\n"}
					Cmd+D or shake for dev menu
				</Text>
				<Video />
			</View>
		);
	}
}

AppRegistry.registerComponent("NMVideoExample", () => NMVideoExample);
