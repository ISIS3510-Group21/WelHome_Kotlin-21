package com.team21.myapplication.ui.createPostView

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.buttons.BorderButton
import com.team21.myapplication.ui.components.buttons.GrayButton
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.inputs.PlaceholderTextField
import com.team21.myapplication.ui.theme.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import com.team21.myapplication.R
import com.team21.myapplication.ui.components.carousel.HorizontalCarousel

@Composable
fun CreatePostScreenLayout() {
    // Principal component as Column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // allows scrolling
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(
                imageVector = AppIcons.ArrowDropDown,//TODO: fix icon
                contentDescription = "Notification",
                tint = BlueCallToAction,
                modifier = Modifier
                    .rotate(90f)
                    .size(32.dp)
            )
            BlackText(
                text = "Create a new post",
                size = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        BlackText(
            text = "Basic Information",
            size = 24.sp,
            fontWeight = FontWeight.Bold
        )

        // "Title" Section
        BlackText(
            text = "Title",
            size = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))
        // Placeholder for "Title"

        PlaceholderTextField(
            placeholderText = "Ex: Cozy Home"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // "Rent" Section
        BlackText(
            text = "Rent (per month)",
            size = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Placeholder for "Rent" section
        PlaceholderTextField(
            placeholderText = "Ex: $ 950.000"
        )
        Spacer(modifier = Modifier.height(16.dp))

        // "Description" Section
        BlackText(
            text = "Description",
            size = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Placeholder for "Description" section
        PlaceholderTextField(
            placeholderText = "Ex: The neighborhood is pretty quiet and nice",
            height = 100.dp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // "Type of housing" Section
        BlackText(
            text = "Type of housing",
            size = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Container for "type of housing" buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Buttons of "Type of housing"
            BorderButton(
                modifier = Modifier.weight(1f),
                text = "House",
                onClick = {},
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.Home,
                        contentDescription = "Add main photo icon",
                        tint = BlueCallToAction
                    )
                }
            )
            BorderButton(
                modifier = Modifier.weight(1f),
                text = "Apartment",
                onClick = {},
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.Apartments,
                        contentDescription = "Add main photo icon",
                        tint = BlueCallToAction
                    )
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BorderButton(
                modifier = Modifier.weight(1f),
                text = "Cabin",
                onClick = {},
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.Cabins,
                        contentDescription = "Add main photo icon",
                        tint = BlueCallToAction
                    )
                }
            )
            BorderButton(
                modifier = Modifier.weight(1f),
                text = "Residence",
                onClick = {},
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.Houses,
                        contentDescription = "Add main photo icon",
                        tint = BlueCallToAction
                    )
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // "Amenities" Section
        BlackText(
            text = "Amenities",
            size = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Containers for buttons of "Amenities" Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Buttons of "Amenities"
            BlueButton(
                modifier = Modifier
                    .height(40.dp)
                    .width(85.dp)
                    .clip(RoundedCornerShape(20.dp)),
                text = "Add",
                onClick = {
                }
            )
            // Horizontal carousel of chips (GrayButton)
            HorizontalCarousel(
                items = listOf("5 Beds", "2 Baths", "70 m2"),
                contentPadding = PaddingValues(horizontal = 0.dp),
                horizontalSpacing = 8.dp,
                snapToItems = false // chips can be partially visible
            ) { label ->
                GrayButton(text = label, onClick = {

                })
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // "Roommates' Profile" Section
        BlackText(
            text = "Roommates' Profile",
            size = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Container for profiles of roommates
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BlueButton(
                modifier = Modifier
                    .height(40.dp)
                    .width(85.dp)
                    .clip(RoundedCornerShape(20.dp)),
                text = "Add",
                onClick = {
                }
            )
            // Horizontal carousel of chips (GrayButton)
            HorizontalCarousel(
                items = listOf("Joan", "Majo", "Arturo Jose"),
                contentPadding = PaddingValues(horizontal = 0.dp),
                horizontalSpacing = 8.dp,
                snapToItems = false // chips can be partially visible
            ) { label ->
                GrayButton(text = label, onClick = {

                })
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // "Add Photos" section
        BlackText(
            text = "Add Photos",
            size = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Placeholders for buttons to upload photos
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BorderButton(
                text = "Add main photo",
                onClick = {},
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.Add,
                        contentDescription = "Add main photo icon",
                        tint = BlueCallToAction
                    )
                }
            )
            BorderButton(
                text = "Take new photos",
                onClick = {},
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.CameraAlt,
                        contentDescription = "Take new photos icon",
                        tint = BlueCallToAction
                    )
                }
            )
            BorderButton(
                text = "Add additional photos",
                onClick = {},
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.Queue,
                        contentDescription = "Add additional photos icon",
                        tint = BlueCallToAction
                    )
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // "Address" section
        BlackText(
            text = "Address",
            size = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Placeholder for address
        PlaceholderTextField(
            placeholderText = "Ex: Cr 9 # XX -XX"
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Map displaying the location
        Image(
            painter = painterResource(id = R.drawable.simple_map),
            contentDescription = "Simple map",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.height(32.dp))

        // "Create" button
        BlueButton(
            text = "Create",
            onClick ={
            }
        )
    }
}

// Preview of the component
@Preview(showBackground = true)
@Composable
fun CreatePostScreenLayoutPreview() {
    CreatePostScreenLayout()
}