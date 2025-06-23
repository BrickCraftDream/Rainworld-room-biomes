import os
import cv2

# Change this to your target directory
directory = "/home/deck/IdeaProjects/Rainworld-MC_Biomes mojamap 1.21.1/src/main/resources/assets/rainworld/textures/block/"

# Supported image extensions
valid_extensions = {'.jpg', '.jpeg', '.png', '.bmp', '.tiff', '.webp'}

# Iterate through each file in the directory
for filename in os.listdir(directory):
    name, ext = os.path.splitext(filename)
    if ext.lower() not in valid_extensions:
        continue

    image_path = os.path.join(directory, filename)
    image = cv2.imread(image_path)

    if image is None:
        print(f"Skipping unreadable file: {filename}")
        continue

    # Convert to HSV
    hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)

    # Set saturation to 0
    hsv[:, :, 1] = 0

    # Convert back to BGR
    whitened_image = cv2.cvtColor(hsv, cv2.COLOR_HSV2BGR)

    # Build output filename
    new_filename = f"{name}_whitened{ext}"
    output_path = os.path.join(directory, new_filename)

    # Save the image
    cv2.imwrite(output_path, whitened_image)
    print(f"Saved: {output_path}")
