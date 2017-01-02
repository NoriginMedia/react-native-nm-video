import React, {Component, PropTypes} from "react";
import {requireNativeComponent, View} from "react-native";

export default class Video extends Component {

	assignRoot(component) {
		this.root = component;
	}

	render() {
		const nativeProps = Object.assign({}, this.props);

		return (
			<RCTNMVideo
				ref={this.assignRoot}
				{...nativeProps}
			/>
		);
	}
}

Video.propTypes = {
	/* Required by react-native */
	scaleX: PropTypes.number,
	scaleY: PropTypes.number,
	translateX: PropTypes.number,
	translateY: PropTypes.number,
	rotation: PropTypes.number,
	...View.propTypes
};

const RCTNMVideo = requireNativeComponent("RCTNMVideo", Video);
